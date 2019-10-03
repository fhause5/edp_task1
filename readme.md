<span style="color: red">&#x1F535; CI/CD into Kubernetes  </span>

### Create kubernetes cluster
* Vagrantfile
* bash script
* jenkins_values
* sonarqube_values

### There are many jenkins-images:
Maven:

jenkins/jnlp-slave:3.27-1

Nodejs:

openshift/jenkins-agent-nodejs-8-centos7:v3.10

Docker:

joao29a/jnlp-slave-alpine-docker:latest

### Add Webhooks

go to project > Setting > Webhooks > http://admin:admin@10.23.7.38:30384/github-webhook/
jenkins:
Configure system > Git Hub server >

![](map.png)
### Look up into the cluster
```
kubectl cluster-info
watch kubectl get all -o wide
```

### Configure nfs
```

sudo yum install nfs-utils git -y
sudo mkdir -p  /srv/nfs/disk1
sudo mkdir /srv/nfs/disk2
sudo mkdir /srv/nfs/disk3
sudo chown -R  nfsnobody:nfsnobody /srv/nfs/
sudo chmod -R 755 /srv/nfs/

sudo tee -a /etc/exports << EOF

/srv/nfs/disk1    *(rw,sync,no_subtree_check,no_root_squash,no_all_squash,insecure)
/srv/nfs/disk2    *(rw,sync,no_subtree_check,no_root_squash,no_all_squash,insecure)
/srv/nfs/disk3    *(rw,sync,no_subtree_check,no_root_squash,no_all_squash,insecure)

EOF
sudo exportfs -rav && sudo exportfs -v

sudo systemctl enable rpcbind

sudo systemctl enable nfs-server

sudo  systemctl enable nfs-lock

sudo systemctl enable nfs-idmap

sudo systemctl start rpcbind

sudo systemctl start nfs-server

sudo systemctl start nfs-lock

sudo systemctl start nfs-idmap

showmount -e 10.23.7.193


kubectl create -f storage_class_nfs.yml rbac.yml deployment_nfs.yml

```

### Install helm and configure tiller

```
curl -L https://git.io/get_helm.sh | bash
kubectl -n kube-system create serviceaccount tiller
kubectl create clusterrolebinding tiller --clusterrole cluster-admin --serviceaccount=kube-system:tiller
helm init --service-account tiller

```
### Install jenkins
```
# Download values
helm inspect values stable/jenkins > /tmp/jenkins.values

# change password
sudo sed -i "s/# adminPassword: <defaults to random>/adminPassword: admin/" /tmp/jenkins.values

# change to nodeport
sudo sed -i "s/serviceType: LoadBalancer/serviceType: NodePort/" /tmp/jenkins.values
nodePort: 32222

installPlugins:
-blueocean:1.18.1
- kubernetes-cd:2.0.0

agent:
image: "joao29a/jnlp-slave-alpine-docker"
tag: "latest"

volumes:
-type: HostPath
 hostPath: /var/run/docker.sock
 mountPath: /var/run/docker.sock



# set NodePort
sudo sed -i "s/# nodePort: <to set explicitly, choose port between 30000-32767/NodePort: 30010/" /tmp/jenkins.values

# add storage class
kubectl get storageclass

# change the size
sudo sed -i "s/size: "8Gi"/size: "5Gi"/" /tmp/jenkins.values

helm install stable/jenkins --name jenkins --values jenkins.values1
helm install stable/sonarqube --name sonarqube --values sonar-values.yaml

### Go to SONARQUBE > Administration > MarketPlace > SonarJava > install
### Go to SONARQUBE > Administration > Configuration > Webhooks
sonarqube-webhook	http://10.23.7.38:30384/sonarqube-webhook/

helm del --purge sonarqube

### Install into jenkins plagins:
### For nexus must have
pipeline-utility-steps
nexus-artifact-uploader
Nexus Platform
SonarQube Scanner
blueocean:1.18.1
kubernetes-cd:2.0.0
workflow-cps
configurationslicing
jquery
Publish Over SSH

### On master server

JENKINS:
http://10.23.13.72:32527/
SONARQUBE:
http://10.23.13.72:31459

```
### Start private docker registry

* systemctl start docker
* docker run -d -p 5000:5000 --restart=always --name registry -v docker:/var/lib/registry registry:2
* docker volume ls

### On workers:
* sudo chmod 666 /var/run/docker.sock

## We can configure multiple docker-registry, However, If we use localhost as docker-registry:
nano /etc/docker/deamon.json
```
{
  "insecure-registries": ["192.168.1.81:5000"]
}

systemctl reload docker
helm install stable/jenkins --name jenkins --values jenkins.values1

```
### manage jenkins > Configure System > kubernetes (Always pull image)

### Jenkins > Credentials > System Global credentials (unrestricted)


## Use monitoring for the cluster
### watch get all -o wide


### Horizontal Pod Autoscale



### JENKINS:
* 10.23.13.231:30008

### Sonarqube:
* 10.23.13.231:30010

### App:
