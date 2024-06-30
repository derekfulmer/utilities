#!/usr/bin/env bash +x

case $ENVIRONMENT in
    dev)
        export AWS_ACCOUNT_ID=240870001965
        export AWS_REGION=us-east-2
        ;;
    stage)
        export AWS_ACCOUNT_ID=275658300116
        export AWS_REGION=us-east-2
        export apple_package_path=ZTMeshMacSysExt-Staging/ZTMesh.pkg
        export windows_package_path=Release-Staging/ZTMesh_Installer.exe
        ;;
    qa)
        export AWS_ACCOUNT_ID=394182880770
        export AWS_REGION=us-east-2
        export apple_package_path=ZTMeshMacSysExt-QA/ZTMesh.pkg
        export windows_package_path=Release/ZTMesh_Installer.exe
        ;;
    demo)
        export AWS_ACCOUNT_ID=572069993803
        export AWS_REGION=us-east-2
        export apple_package_path=ZTMeshMacSysExt-Demo/ZTMesh.pkg
        export windows_package_path=Release-Demo/ZTMesh_Installer.exe
        ;;
    prod)
        export AWS_ACCOUNT_ID=378420464546
        export AWS_REGION=us-east-2
        export package_path=ZTMeshMacSysExt/ZTMesh.pkg
        export package_path=Release/ZTMesh_Installer.exe
        ;;
esac

aws sts get-caller-identity
echo AssumeRole ..................................
ASSUME_ROLE_OUTPUT=$(aws sts assume-role --role-arn "arn:aws:iam::${AWS_ACCOUNT_ID}:role/devops-runner" --role-session-name jenkins)
export AWS_ACCESS_KEY_ID=$(echo $ASSUME_ROLE_OUTPUT | jq -r '.Credentials.AccessKeyId')
export AWS_SECRET_ACCESS_KEY=$(echo $ASSUME_ROLE_OUTPUT | jq -r '.Credentials.SecretAccessKey')
export AWS_SESSION_TOKEN=$(echo $ASSUME_ROLE_OUTPUT | jq -r '.Credentials.SessionToken')
echo Check role ....................................
aws sts get-caller-identity

#get repo creds to authn
reposecret=$(aws secretsmanager get-secret-value --secret-id "devops/repo-devops-service" --query "SecretString" --output text --region us-east-2)
repoid=$(echo "$reposecret" | jq .repo_devops_token_id | tr -d '"')
repotokenpw=$(echo "$reposecret" | jq .repo_devops_token_pw | tr -d '"')


repo_url=$(echo https://repo.optm.network/repository/ztmesh-client-"$repository_name"/"$release_tag"/"$package_path")

curl -X 'GET' -u "$repoid":"$repotokenpw" -O "$repo_url" -H 'accept: application/json'
ls -lah

if [ "$repository_name" = 'ztmesh-apple' ]; then
	client_file=ZTMesh.pkg
else
	client_file=ZTMesh_Installer.exe
fi
echo "$client_file"

s3_url=$(echo s3://resources-"$ENVIRONMENT".cyberight.net/"$repository_name"/"$release_tag"/)
aws s3 cp "$client_file" "$s3_url"

version_file='version.txt'
echo "$repository_name"-"$release_tag" > "$version_file"

aws s3 cp "$version_file" "$s3_url"


