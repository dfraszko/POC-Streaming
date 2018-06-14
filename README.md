Akka stream
===========

- [Documentation](https://doc.akka.io/docs/akka/2.5/stream/index.html)

Tcomp
-----

```shell
cd deploy/poc-streaming
docker-compose up -d
```

### Fetch data

```shell
curl \
  --verbose \
  --request POST \
  --header 'Accept: application/avro-json' \
  --header 'Content-Type: application/json' \
  --data '{"properties":{"format":"AVRO","path":"/data/example-1k.avro","@definitionName":"SimpleFileIoDataset"},"dependencies":[{"@definitionName":"SimpleFileIoDatastore"}]}' \
  'http://127.0.0.1:8989/tcomp/v0/runtimes/data' > example-1k.avro
```

### Fetch schema

```shell
curl \
  --verbose \
  --request POST \
  --header 'Accept: application/json' \
  --header 'Content-Type: application/json' \
  --data '{"properties":{"format":"AVRO","path":"/data/example-1k.avro","@definitionName":"SimpleFileIoDataset"},"dependencies":[{"@definitionName":"SimpleFileIoDatastore"}]}' \
  'http://127.0.0.1:8989/tcomp/v0/runtimes/schema' > example-1k.avsc
```

POC
---

### Storage

```shell
curl --verbose --request POST --header 'Content-Type: application/octet-stream' -d@example-1k.avro http://127.0.0.1:8080/storage/1

curl --verbose --request GET http://127.0.0.1:8080/storage/1 > result.avro
```

### Relay (retrieve full dataset)

```shell
curl --verbose --header 'Accept: application/avro-json' --request GET http://127.0.0.1:8080/relay/example-1k.avro > result-json.avro

curl --verbose --header 'Accept: application/avro-binary' --request GET http://127.0.0.1:8080/relay/example-1k.avro > result-binary.avro

curl --verbose --request GET http://127.0.0.1:8080/dataset/example-1k.avro > result-binary.avro
```
