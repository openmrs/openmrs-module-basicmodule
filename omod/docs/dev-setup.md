
## Services:
Clone the basic 5 services:
- [OMOD-HIP](https://github.com/Bahmni-Covid19/openmrs-module-hip.git)
- [Hip Service](https://github.com/Bahmni-Covid19/hip-service)
- [BahmniApps](https://github.com/Bahmni-Covid19/openmrs-module-bahmniapps)
- [Ndhm-React](https://github.com/Bahmni-Covid19/ndhm-react)
- [Default-config](https://github.com/Bahmni-Covid19/default-config)

Others can be found here : [Bahmni-Covid19](https://github.com/Bahmni-Covid19/)
###Setup Bahmni using Vagrant

1. Install [Virtualbox](https://www.virtualbox.org/wiki/Downloads)
2. Install [Vagrant](https://www.vagrantup.com/downloads.html) 
3. Clone the `bahmni-vagrant` repo from github inside a folder called `bahmni`

   ```bash
   mkdir bahmni
   cd bahmni
   git clone https://github.com/Bahmni/bahmni-vagrant.git
   cd bahmni-vagrant
   ```

4. Replace the contents of the `Vagrantfile` in `bahmni-vagrant` with the following:
    
   ```bash
    Vagrant.configure(2) do |config|
      config.vm.box = "bento/centos-7.6"
      config.vm.box_check_update = true
      config.ssh.insert_key = false
      config.vm.network "private_network", ip: "192.168.33.10"
      config.vm.network "forwarded_port", guest: 3306, host: 3306
      config.vm.network "forwarded_port", guest: 8000, host: 8000
      config.vm.network "forwarded_port", guest: 80, host: 80
    
      config.vm.synced_folder "..", "/bahmni", :owner => "vagrant"
      config.vm.provider "virtualbox" do |v|
         v.customize ["modifyvm", :id, "--memory", 3092, "--cpus", 2, "--name", "Bahmni-RPM"]
      end
    end
   ```
   
5. Make vagrant up and open vagrant
    
   ```
    vagrant up && vagrant ssh
   ```

6. Now, inside the vagrant box set up a local Bahmni instance by using this document
(Note: install the version 0.92 provided in the [link](https://bahmni.atlassian.net/wiki/spaces/BAH/pages/33128505/Install+Bahmni+on+CentOS))

7. Open [bahmni-emr-login](https://192.168.33.10/bahmni/home/index.html#/login), you should see bahmni up and running.  
    - Username: superman
    - Password: Admin123

###Prerequisites for OMOD-HIP:
   
- Download following jars and fhir
    - [jackson-core-2.10.0.jar](https://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-core/2.10.0/jackson-core-2.10.0.jar)
    - [jackson-annotations-2.10.0.jar](https://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-annotations/2.10.0/jackson-annotations-2.10.0.jar)
    - [jackson-databind-2.10.0.jar](https://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-databind/2.10.0/jackson-databind-2.10.0.jar)
    - [fhir2-omod-1.0.0-20200925.143534-163.jar](https://openmrs.jfrog.io/artifactory/public/org/openmrs/module/fhir2-omod/1.0.0-SNAPSHOT/fhir2-omod-1.0.0-20200925.143534-163.jar)
- Rename `fhir2-omod-1.0.0-20200925.143534-163.jar` file from `.jar` to `.omod` extension.
- Copy all fies to `bahmni` folder
- Open vagrant and paste the files into `/opt/openmrs/modules/` directory
  
  ```bash
  cd bahmni-vagrant
  vagrant ssh
  cp /bahmni/jackson-core-2.10.0.jar /opt/openmrs/modules/
  cp /bahmni/jackson-annotations-2.10.0.jar /opt/openmrs/modules/
  cp /bahmni/jackson-databind-2.10.0.jar /opt/openmrs/modules/
  cp /bahmni/fhir2-omod-1.0.0-20200925.143534-163.mod /opt/openmrs/modules/
  ```  
- Restart openmrs `systemctl restart openmrs`

- Ensure that the module is successfully loaded by navigating through [openmrs](http://192.168.33.10/openmrs)
    - username: superman
    - password: Admin123
    - Click on `Administration` -> `Manage Modules` under `Modules` 
- You can also check the log file for any errors/exceptions: `vi /opt/openmrs/openmrs.log`
 in vagrant. 
 
###Setting up the OMOD-HIP

(Make sure `bahmni` folder is created and `bahmni vagrant` is setup inside `bahmni`)
1. Clone OMOD-HIP

    ```
    cd bahmni
    git clone https://github.com/Bahmni-Covid19/openmrs-module-hip.git
   ```

2. Run the steps to build .omod file

    ```
    cd openmrs-module-hip
    mvn clean install
    ```

3. Move the newly build omod to vagrant and overwrite  it and restart OMOD with the new omod

    ```
    vagrant ssh
    sudo su
    cp /bahmni/openmrs-module-hip/omod/target/hipmodule-omod-0.1-SNAPSHOT.omod /opt/openmrs/modules/
    systemctl restart openmrs
    ```

4. (Optional) Check log file to see if any errors occurred inside vagrant

    ```
    vi /opt/openmrs/openmrs.log

    ```
