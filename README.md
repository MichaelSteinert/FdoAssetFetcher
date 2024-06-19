# About this repository

It contains a proof-of-concept for the FDO-EDC adapter. For demonstration and testing purposes a minimal provider connector service is included as well.

## Minimal Asset Provider Service

- Allows to create example assets which then can be fetched by the "App" and published as FDO.

### 1. Build the connector

```bash
./gradlew connector:build
```

### 2. Run the connector

```bash
java -Dedc.keystore=connector/resources/certs/cert.pfx \
     -Dedc.keystore.password=123456 \
     -Dedc.vault=connector/resources/configuration/provider-vault.properties \
     -Dedc.fs.config=connector/resources/configuration/provider-configuration.properties \
     -jar connector/build/libs/connector.jar
```

### 3. Create an asset

```bash
curl -d '{
    "@context": {
        "@vocab": "https://w3id.org/edc/v0.0.1/ns/"
    },
    "@id": "myAssetId",
    "properties": {
        "name": "Product EDC Demo Asset",
        "contenttype": "application/json",
        "description": "Product Description about Test Asset",
        "isFdo": "true"
    },
    "dataAddress": {
        "type": "HttpData",
        "baseUrl": "https://jsonplaceholder.typicode.com/todos",
        "proxyPath": "true"
    }
}' \
  -H 'content-type: application/json' http://localhost:19193/management/v3/assets \
  -s
```

### 4. Create a policy

```bash
curl -d '{
    "@context": {
        "@vocab": "https://w3id.org/edc/v0.0.1/ns/",
        "odrl": "http://www.w3.org/ns/odrl/2/"
    },
    "@id": "PolicyForTestAsset",
    "policy": {
        "@context": "http://www.w3.org/ns/odrl.jsonld",
        "@type": "Set",
        "permission": [],
        "prohibition": [],
        "obligation": []
    }
}' \
  -H 'content-type: application/json' http://localhost:19193/management/v2/policydefinitions \
  -s
```

### 5. Create a contract definition

```bash
curl -d '{
  "@context": {
    "@vocab": "https://w3id.org/edc/v0.0.1/ns/"
  },
  "@id": "ContractDefinitionForTestAsset",
  "accessPolicyId": "PolicyForTestAsset",
  "contractPolicyId": "PolicyForTestAsset",
  "assetsSelector": []
}' \
  -H 'content-type: application/json' http://localhost:19193/management/v2/contractdefinitions \
  -s
```

### 6. Fetch catalog

For manual testing.

```bash
curl -d '{
    "@context": {
        "@vocab": "https://w3id.org/edc/v0.0.1/ns/"
    },
    "counterPartyAddress": "http://localhost:19194/protocol",
    "protocol": "dataspace-protocol-http"
}' \
  -H 'content-type: application/json' http://localhost:19193/management/v2/catalog/request \
  -s
```

## FDO-EDC Adapter ("App")

Fetches the catalog of assets. If there are new assets marked as `isFdo=true`, they will be published as FDO.

### Configure the FDO Repository where the FDOs will be created.

* Store a valid repository config json to `app/.test.linkahead.json` [Example](https://gitlab.indiscale.com/fdo/fdo-manager-library/-/blob/main/.test.linkahead.json.example)

### Start the App

* We assume that the Asset Provider Service is running because the connection details are hard-coded right now.
* We skip tests because they don't seem to be passing right now.

```bash
./gradlew run -x test
```
