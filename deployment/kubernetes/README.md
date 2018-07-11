# Black Duck Alert On Kubernetes.

## Requirements

### Installing Alert

### Standalone Installation
All below commands assume:
- you are using the namespace (or openshift project name) 'blackduck-alert'.
- you have a cluster with at least 1GB of allocatable memory.
- you have administrative access to your cluster.

#### Step 1: Create the configuration map for Alert

Create the config map from the file to specify the environment variables for Alert

```
kubectl create -f 1-cm-alert.yml -n blackduck-alert
```

#### Step 2: Create your cfssl container, and the Alert config map

Setup the cfssl container for managing certificates

```
kubectl create -f 2-cfssl.yml -n blackduck-alert
```

#### Step 3: Create the Alert application's containers
Now setup the Alert containers for the application

```
kubectl create -f 3-alert.yml -n blackduck-alert
```

##### Quick start
 For your convenience 
 * Create Alert deployment:
    * Execute the bundled create.sh file.  It will create a blackduck-alert namespace and deploy the application.
    * Different namespace: create.sh <your_alert_namespace>
 * Delete Alert Deployment:
    * Execute the bundled delete.sh file.  It will delete the deployment in the blackduck-alert namespace.
    * Different namespace: delete.sh <your_alert_namespace>

### Installation with the Hub

You can install the Alert container as part of you Hub installation.  This section describes the steps to install Alert with the Hub.

#### Step 1: Install the Hub
 * During installation update the hub-config Configuration Map to have the variable USE_ALERT: "1"

#### Step 2: Create the configuration map for Alert

```
kubectl create -f 2-cm-alert.yml -n <your_hub_namespace>
```

#### Step 3: Create the Alert application's container

```
kubectl create -f 3-alert.yml -n <your_hub_namespace>
```

## Using a Custom web server certificate-key pair

Alert allows users to use their own web server certificate-key pairs for establishing ssl connection.

* Create a Kubernetes secret each called 'WEBSERVER_CUSTOM_CERT_FILE' and 'WEBSERVER_CUSTOM_KEY_FILE' with the custom certificate and custom key in your namespace.

You can do so by

```
kubectl secret create WEBSERVER_CUSTOM_CERT_FILE --from-file=<certificate file>
kubectl secret create WEBSERVER_CUSTOM_KEY_FILE --from-file=<key file>
```

For the webserver service, add secrets by copying their values into 'env'
values for the pod specifications in the webserver.