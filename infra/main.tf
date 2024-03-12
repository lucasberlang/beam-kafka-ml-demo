
provider "google" {
  project = var.project_id
  region  = var.region
}

module "cloud_storage" {
  source = "git::https://github.com/lucasberlang/gcp-cloud-storage.git?ref=v1.4.0"

  name          = var.name
  offset        = var.offset
  project_id    = var.project_id
  force_destroy = var.force_destroy

  tags = var.tags
}

module "bq" {
  source = "git::https://github.com/lucasberlang/gcp-bigquery.git?ref=v1.5.0"

  id         = var.id
  offset     = var.offset_bq
  project_id = var.project_id
  tables = {
    "kafka-sink-table" = {
      friendly_name       = "kafka-sink-table"
      schema              = data.local_file.kafka_table.content
      deletion_protection = false
    },
  }
  tags = var.tags
}

resource "google_monitoring_notification_channel" "notification" {
  display_name = "Lucas Calvo"
  type         = "email"
  labels = {
    email_address = "lucas.calvoo@bluetab.net"
  }
  force_delete = false
}

resource "google_monitoring_alert_policy" "alert_error_streaming" {
  display_name          = "DataFlow streaming-beam Failed"
  combiner              = "OR"
  notification_channels = [google_monitoring_notification_channel.notification.name]
  project               = var.project_id
  documentation {
    content = "Ha habido un error en la pipeline de dataflow: streaming-beam"
  }

  conditions {
    display_name = "Fail stream-beam"
    condition_matched_log {
      filter = "resource.type=\"dataflow_step\"\nlogName=\"projects/practica-cloud-286009/logs/dataflow.googleapis.com%2Fjob-message\"\nseverity=ERROR\nresource.labels.project_id=\"827851015670\"\nresource.labels.job_name=~\"streaming-beam\""
    }
  }

  alert_strategy {
    auto_close = "1800s"
    notification_rate_limit {
      period = "1800s"
    }
  }
  user_labels = var.tags
}

resource "google_monitoring_dashboard" "dashboard" {
  dashboard_json = <<EOF
  {
  "dashboardFilters": [],
  "displayName": "CustomDataflowTerraform",
  "labels": {},
  "mosaicLayout": {
    "columns": 48,
    "tiles": [
      {
        "height": 16,
        "widget": {
          "title": "Failed [MEAN]",
          "xyChart": {
            "chartOptions": {
              "mode": "COLOR"
            },
            "dataSets": [
              {
                "minAlignmentPeriod": "60s",
                "plotType": "LINE",
                "targetAxis": "Y1",
                "timeSeriesQuery": {
                  "timeSeriesFilter": {
                    "aggregation": {
                      "alignmentPeriod": "60s",
                      "crossSeriesReducer": "REDUCE_MEAN",
                      "groupByFields": [
                        "resource.label.\"job_name\""
                      ],
                      "perSeriesAligner": "ALIGN_MEAN"
                    },
                    "filter": "metric.type=\"dataflow.googleapis.com/job/is_failed\" resource.type=\"dataflow_job\""
                  }
                }
              }
            ],
            "thresholds": [],
            "yAxis": {
              "label": "",
              "scale": "LINEAR"
            }
          }
        },
        "width": 24
      },
      {
        "height": 16,
        "widget": {
          "title": "System lag [MEAN]",
          "xyChart": {
            "chartOptions": {
              "mode": "COLOR"
            },
            "dataSets": [
              {
                "minAlignmentPeriod": "60s",
                "plotType": "LINE",
                "targetAxis": "Y1",
                "timeSeriesQuery": {
                  "timeSeriesFilter": {
                    "aggregation": {
                      "alignmentPeriod": "60s",
                      "crossSeriesReducer": "REDUCE_MEAN",
                      "groupByFields": [
                        "resource.label.\"job_name\""
                      ],
                      "perSeriesAligner": "ALIGN_MEAN"
                    },
                    "filter": "metric.type=\"dataflow.googleapis.com/job/system_lag\" resource.type=\"dataflow_job\""
                  }
                }
              }
            ],
            "thresholds": [],
            "yAxis": {
              "label": "",
              "scale": "LINEAR"
            }
          }
        },
        "width": 24,
        "xPos": 24
      },
      {
        "height": 16,
        "widget": {
          "title": "Current number of vCPUs in use [MEAN]",
          "xyChart": {
            "chartOptions": {
              "mode": "COLOR"
            },
            "dataSets": [
              {
                "minAlignmentPeriod": "60s",
                "plotType": "LINE",
                "targetAxis": "Y1",
                "timeSeriesQuery": {
                  "timeSeriesFilter": {
                    "aggregation": {
                      "alignmentPeriod": "60s",
                      "crossSeriesReducer": "REDUCE_MEAN",
                      "groupByFields": [
                        "resource.label.\"job_name\""
                      ],
                      "perSeriesAligner": "ALIGN_MEAN"
                    },
                    "filter": "metric.type=\"dataflow.googleapis.com/job/current_num_vcpus\" resource.type=\"dataflow_job\""
                  }
                }
              }
            ],
            "thresholds": [],
            "yAxis": {
              "label": "",
              "scale": "LINEAR"
            }
          }
        },
        "width": 24,
        "yPos": 16
      },
      {
        "height": 16,
        "widget": {
          "title": "Elements Produced [SUM]",
          "xyChart": {
            "chartOptions": {
              "mode": "COLOR"
            },
            "dataSets": [
              {
                "minAlignmentPeriod": "60s",
                "plotType": "LINE",
                "targetAxis": "Y1",
                "timeSeriesQuery": {
                  "timeSeriesFilter": {
                    "aggregation": {
                      "alignmentPeriod": "60s",
                      "crossSeriesReducer": "REDUCE_SUM",
                      "groupByFields": [
                        "resource.label.\"job_name\""
                      ],
                      "perSeriesAligner": "ALIGN_RATE"
                    },
                    "filter": "metric.type=\"dataflow.googleapis.com/job/elements_produced_count\" resource.type=\"dataflow_job\""
                  }
                }
              }
            ],
            "thresholds": [],
            "yAxis": {
              "label": "",
              "scale": "LINEAR"
            }
          }
        },
        "width": 24,
        "xPos": 24,
        "yPos": 16
      },
      {
        "height": 16,
        "widget": {
          "title": "Storage bytes read [SUM]",
          "xyChart": {
            "chartOptions": {
              "mode": "COLOR"
            },
            "dataSets": [
              {
                "minAlignmentPeriod": "60s",
                "plotType": "LINE",
                "targetAxis": "Y1",
                "timeSeriesQuery": {
                  "timeSeriesFilter": {
                    "aggregation": {
                      "alignmentPeriod": "60s",
                      "crossSeriesReducer": "REDUCE_SUM",
                      "groupByFields": [],
                      "perSeriesAligner": "ALIGN_RATE"
                    },
                    "filter": "metric.type=\"dataflow.googleapis.com/job/streaming_engine/persistent_state/read_bytes_count\" resource.type=\"dataflow_job\""
                  }
                }
              }
            ],
            "thresholds": [],
            "yAxis": {
              "label": "",
              "scale": "LINEAR"
            }
          }
        },
        "width": 24,
        "yPos": 32
      },
      {
        "height": 16,
        "widget": {
          "title": "BigQueryIO.Write Requests [SUM]",
          "xyChart": {
            "chartOptions": {
              "mode": "COLOR"
            },
            "dataSets": [
              {
                "minAlignmentPeriod": "60s",
                "plotType": "LINE",
                "targetAxis": "Y1",
                "timeSeriesQuery": {
                  "timeSeriesFilter": {
                    "aggregation": {
                      "alignmentPeriod": "60s",
                      "crossSeriesReducer": "REDUCE_SUM",
                      "groupByFields": [
                        "resource.label.\"job_name\""
                      ],
                      "perSeriesAligner": "ALIGN_RATE"
                    },
                    "filter": "metric.type=\"dataflow.googleapis.com/job/bigquery/write_count\" resource.type=\"dataflow_job\""
                  }
                }
              }
            ],
            "thresholds": [],
            "yAxis": {
              "label": "",
              "scale": "LINEAR"
            }
          }
        },
        "width": 24,
        "xPos": 24,
        "yPos": 32
      }
    ]
  }
}

  EOF
  project        = var.project_id
}
