terraform {
  required_providers {
    newrelic = {
      source  = "newrelic/newrelic"
      version = "~> 3.30"
    }
  }
}

provider "newrelic" {
  account_id = var.newrelic_account_id
  api_key    = var.newrelic_api_key
  region     = var.newrelic_region
}

variable "newrelic_account_id" {
  description = "New Relic Account ID"
  type        = string
}

variable "newrelic_api_key" {
  description = "New Relic User API Key"
  type        = string
  sensitive   = true
}

variable "newrelic_region" {
  description = "New Relic region (US or EU)"
  type        = string
  default     = "US"
}

variable "notification_email" {
  description = "Email address for alert notifications"
  type        = string
  default     = "oncall@example.com"
}

# --- Alert Policy ---

resource "newrelic_alert_policy" "oncall_demo" {
  name                = "OnCall Demo - Order Processing Errors"
  incident_preference = "PER_CONDITION"
}

# --- NRQL Alert: NullPointerException in Order Processing ---

resource "newrelic_nrql_alert_condition" "null_pointer_exception" {
  account_id                   = var.newrelic_account_id
  policy_id                    = newrelic_alert_policy.oncall_demo.id
  type                         = "static"
  name                         = "NullPointerException in oncall-demo"
  description                  = "A NullPointerException occurred in the Order Processing service. User shipping address is null."
  enabled                      = true
  violation_time_limit_seconds = 3600

  nrql {
    query = "SELECT count(*) FROM TransactionError WHERE appName = 'oncall-demo' AND error.class = 'java.lang.NullPointerException'"
  }

  critical {
    operator              = "above_or_equals"
    threshold             = 1
    threshold_duration    = 60
    threshold_occurrences = "at_least_once"
  }

  fill_option = "none"
}

# --- NRQL Alert: High Error Rate ---

resource "newrelic_nrql_alert_condition" "high_error_rate" {
  account_id                   = var.newrelic_account_id
  policy_id                    = newrelic_alert_policy.oncall_demo.id
  type                         = "static"
  name                         = "High Error Rate - oncall-demo"
  description                  = "Error rate exceeded 5% on the oncall-demo application."
  enabled                      = true
  violation_time_limit_seconds = 3600

  nrql {
    query = "SELECT percentage(count(*), WHERE error IS true) FROM Transaction WHERE appName = 'oncall-demo'"
  }

  critical {
    operator              = "above"
    threshold             = 5
    threshold_duration    = 300
    threshold_occurrences = "all"
  }

  warning {
    operator              = "above"
    threshold             = 2
    threshold_duration    = 300
    threshold_occurrences = "all"
  }

  fill_option = "none"
}

# --- Notification Channel ---

resource "newrelic_notification_destination" "email" {
  name = "OnCall Demo Email"
  type = "EMAIL"

  property {
    key   = "email"
    value = var.notification_email
  }
}

resource "newrelic_notification_channel" "email_channel" {
  name           = "OnCall Demo Email Channel"
  type           = "EMAIL"
  destination_id = newrelic_notification_destination.email.id
  product        = "IINT"

  property {
    key   = "subject"
    value = "{{issueTitle}}"
  }
}

resource "newrelic_workflow" "oncall_demo_workflow" {
  name                  = "OnCall Demo Alert Workflow"
  muting_rules_handling = "NOTIFY_ALL_ISSUES"

  issues_filter {
    name = "oncall-demo-filter"
    type = "FILTER"

    predicate {
      attribute = "labels.policyIds"
      operator  = "EXACTLY_MATCHES"
      values    = [newrelic_alert_policy.oncall_demo.id]
    }
  }

  destination {
    channel_id = newrelic_notification_channel.email_channel.id
  }
}

# --- Outputs ---

output "alert_policy_id" {
  value       = newrelic_alert_policy.oncall_demo.id
  description = "New Relic Alert Policy ID"
}

output "npe_condition_id" {
  value       = newrelic_nrql_alert_condition.null_pointer_exception.id
  description = "NullPointerException Alert Condition ID"
}
