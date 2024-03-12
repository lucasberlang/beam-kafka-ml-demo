
######
# google_bigquery_dataset
######

output "dataset" {
  description = "Dataset resource."
  value       = module.bq.dataset
}

output "dataset_id" {
  description = "Dataset id."
  value       = module.bq.dataset_id
}

output "id" {
  description = "Fully qualified dataset id."
  value       = module.bq.id
}

output "self_link" {
  description = "Dataset self link."
  value       = module.bq.self_link
}

######
# google_bigquery_table
######

output "table_ids" {
  description = "Map of fully qualified table ids keyed by table ids."
  value       = module.bq.table_ids
}

output "tables" {
  description = "Table resources."
  value       = module.bq.tables
}
