# Google Dataflow to Bigquery Project

Terraform project which creates a datasets and tables in BigQuery and monitoring to dataflow job.

* [GCP Bigquery Dataset](https://www.terraform.io/docs/providers/google/r/bigquery_dataset.html)
* [GCP Bigquery Table](https://www.terraform.io/docs/providers/google/r/bigquery_table.html)
* [GCP Dataset Access](https://registry.terraform.io/providers/hashicorp/google/latest/docs/resources/bigquery_dataset_access)

## Terraform versions

Supported version 0.15 and higher.

## Usage

## Environment Setup

1. Set up your Google Cloud environment:

    ```bash
    gcloud init
    export GOOGLE_APPLICATION_CREDENTIALS="/path/key.json"
    ```

2. Execute terraform to create infrastructure:

    ```bash
    terraform init
    terraform apply
    ```

3. Clean all resources:

    ```bash
    terraform destroy
    ```

<!-- BEGINNING OF PRE-COMMIT-TERRAFORM DOCS HOOK -->
## Requirements

| Name | Version |
|------|---------|
| <a name="requirement_terraform"></a> [terraform](#requirement\_terraform) | >= 0.15 |
| <a name="requirement_google"></a> [google](#requirement\_google) | ~> 4.0 |
| <a name="requirement_google-beta"></a> [google-beta](#requirement\_google-beta) | ~> 4.0 |

## Providers

| Name | Version |
|------|---------|
| <a name="provider_google"></a> [google](#provider\_google) | 4.84.0 |
| <a name="provider_local"></a> [local](#provider\_local) | 2.4.1 |

## Modules

| Name | Source | Version |
|------|--------|---------|
| <a name="module_bq"></a> [bq](#module\_bq) | git::ssh://git@gitlab.bluetab.net/terraform/modules/google/gcp-bigquery.git | v1.5.0 |
| <a name="module_cloud_storage"></a> [cloud\_storage](#module\_cloud\_storage) | git::ssh://git@gitlab.bluetab.net/terraform/modules/google/gcp-cloud-storage.git | v1.4.0 |

## Resources

| Name | Type |
|------|------|
| [google_monitoring_alert_policy.alert_error_streaming](https://registry.terraform.io/providers/hashicorp/google/latest/docs/resources/monitoring_alert_policy) | resource |
| [google_monitoring_dashboard.dashboard](https://registry.terraform.io/providers/hashicorp/google/latest/docs/resources/monitoring_dashboard) | resource |
| [google_monitoring_notification_channel.notification](https://registry.terraform.io/providers/hashicorp/google/latest/docs/resources/monitoring_notification_channel) | resource |
| [local_file.kafka_table](https://registry.terraform.io/providers/hashicorp/local/latest/docs/data-sources/file) | data source |

## Inputs

| Name | Description | Type | Default | Required |
|------|-------------|------|---------|:--------:|
| <a name="input_force_destroy"></a> [force\_destroy](#input\_force\_destroy) | Optional map to set force destroy keyed by name, defaults to false. | `bool` | n/a | yes |
| <a name="input_id"></a> [id](#input\_id) | Dataset id. | `string` | n/a | yes |
| <a name="input_name"></a> [name](#input\_name) | Cloud storage name. | `string` | n/a | yes |
| <a name="input_offset"></a> [offset](#input\_offset) | The offset to be added to the google cloud storage | `number` | n/a | yes |
| <a name="input_offset_bq"></a> [offset\_bq](#input\_offset\_bq) | The offset to be added to the bigquery dataset | `number` | `1` | no |
| <a name="input_project_id"></a> [project\_id](#input\_project\_id) | fixture | `string` | n/a | yes |
| <a name="input_region"></a> [region](#input\_region) | fixture | `string` | `"europe-west4"` | no |
| <a name="input_tags"></a> [tags](#input\_tags) | fixture | `map(string)` | n/a | yes |

## Outputs

| Name | Description |
|------|-------------|
| <a name="output_dataset"></a> [dataset](#output\_dataset) | Dataset resource. |
| <a name="output_dataset_id"></a> [dataset\_id](#output\_dataset\_id) | Dataset id. |
| <a name="output_id"></a> [id](#output\_id) | Fully qualified dataset id. |
| <a name="output_self_link"></a> [self\_link](#output\_self\_link) | Dataset self link. |
| <a name="output_table_ids"></a> [table\_ids](#output\_table\_ids) | Map of fully qualified table ids keyed by table ids. |
| <a name="output_tables"></a> [tables](#output\_tables) | Table resources. |
<!-- END OF PRE-COMMIT-TERRAFORM DOCS HOOK -->
