#Download latest HIP OMOD and relaunch openmrs
username=***
token=****
list_artifacts=https://api.github.com/repos/Bahmni-Covid19/openmrs-module-hip/actions/artifacts
omod_file=hipmodule-omod-0.1-SNAPSHOT.omod

source /root/deployScripts/artifacts/omod-hip

#Get artifact's download URL from the List artifact endpoint
artifact_id=$(curl "${list_artifacts}" | jq ".artifacts[0].id")
artifact_url=$(curl "${list_artifacts}" | jq ".artifacts[0].archive_download_url" | sed s/\"//g)

if [ "$PUBLISHED_HIP_ARTIFACTID" != "$artifact_id" ]
then
#Download the artifact
curl -L -o package_${artifact_id}.zip -u${username}:${token} ${artifact_url}

#Remove old HIP OMOD from /opt/openmrs/modules
rm -rf /opt/openmrs/modules/${omod_file}

#Unzip new package
mkdir ./package_${artifact_id}
unzip -d ./package_${artifact_id} package_${artifact_id}.zip
cp package_${artifact_id}/${omod_file} /opt/openmrs/modules
chmod 777 /opt/openmrs/modules/${omod_file}
chown bahmni:bahmni /opt/openmrs/modules/${omod_file}

# Restart Openmrs
systemctl restart openmrs

# Cleanup
rm -f ./package_${artifact_id}.zip
rm -rf ./package_${artifact_id}

echo "PUBLISHED_HIP_ARTIFACTID=${artifact_id}" > /root/deployScripts/artifacts/omod-hip
else
echo "Artifaact didnt change"
fi
~
