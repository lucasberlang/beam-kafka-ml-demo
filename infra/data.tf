
data "local_file" "kafka_table" {
  filename = "${path.module}/templates/tables/kafka_table.json"
}
