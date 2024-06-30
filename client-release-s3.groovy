pipeline {
  agent any
  /* choiceParam a problem with automation
  parameters {
    choiceParam("ENVIRONMENT", ["dev", "qa", "stage", "prod"], "The deployment environment.")
	choiceParam("repo_env", ["Dev", "Qa", "Staging", "Prod"], "The release environment for the ZTMesh client installer. Note these are case-sensitive and specific to their naming in Nexus Repository and may differ from the ENVIRONMENT variable; e.g. Staging vs Stage.")
  }
  */
  triggers {
    GenericTrigger(
     genericVariables: [
      [key: 'repo', value: '$.repository.name'],
      [key: 'relase', value: '$.repository.releases_url'],
      [key: 'committer', value: '$.head_commit.committer.username']
     ],

     causeString: 'Triggered by $committer_name on release of $repo_name $release_tag',

     //token: 'abc123',
     //tokenCredentialId: '',

     printContributedVariables: true,
     printPostContent: true,

     silentResponse: false,
     
     shouldNotFlatten: false,

     //regexpFilterText: '$ref',
     //regexpFilterExpression: 'refs/heads/' + BRANCH_NAME
    )
  }
  stages {
    stage('Preflight check') {
      steps {
        sh "echo $reponame $release_tag pushed by $commiter_name"
      }
    }
    stage('Client and version stuff') {
        steps {
            sh '''
            #!/usr/bin/env bash +x
case \$ENVIRONMENT in
    dev)
        export AWS_ACCOUNT_ID=240870001965
        export AWS_REGION=us-east-2
        export CH=de
        ;;
    stage)
        export AWS_ACCOUNT_ID=275658300116
        export AWS_REGION=us-east-2
        export CH=st
        ;;
    qa)
        export AWS_ACCOUNT_ID=394182880770
        export AWS_REGION=us-east-2
        export CH=qa
        ;;
    demo)
        export AWS_ACCOUNT_ID=572069993803
        export AWS_REGION=us-east-2
        export CH=dm
        ;;
    prod)
        export AWS_ACCOUNT_ID=378420464546
        export AWS_REGION=us-east-2
        export CH=pr
        ;;
esac

export "\$ENVIRONMENT"
export "\$repository_name"
export "\$release_tag"
export "\$repo_env"

aws sts get-caller-identity
echo AssumeRole ..................................
ASSUME_ROLE_OUTPUT=\$(aws sts assume-role --role-arn "arn:aws:iam::\${AWS_ACCOUNT_ID}:role/devops-runner" --role-session-name jenkins)
export AWS_ACCESS_KEY_ID=\$(echo \$ASSUME_ROLE_OUTPUT | jq -r '.Credentials.AccessKeyId')
export AWS_SECRET_ACCESS_KEY=\$(echo \$ASSUME_ROLE_OUTPUT | jq -r '.Credentials.SecretAccessKey')
export AWS_SESSION_TOKEN=\$(echo \$ASSUME_ROLE_OUTPUT | jq -r '.Credentials.SessionToken')
echo Check role ....................................
aws sts get-caller-identity

#get repo creds to authn
reposecret=\$(aws secretsmanager get-secret-value --secret-id "devops/repo-devops-service" --query "SecretString" --output text --region us-east-2)
repoid=\$(echo "\$reposecret" | jq .repo_devops_token_id | tr -d '"')
repotokenpw=\$(echo "\$reposecret" | jq .repo_devops_token_pw | tr -d '"')

case \$repo_env in
    Staging)
        if [ "\$repository_name" = 'ztmesh-apple' ]; then
            export package_path=ZTMeshMacSysExt-Staging/ZTMesh.pkg
        else [ "\$repository_name" = 'ztmesh-windows' ]
            export package_path=Release-Staging/ZTMesh_Installer.exe
        fi
        ;;
    Qa)
        if [ "\$repository_name" = 'ztmesh-apple' ]; then
            export package_path=ZTMeshMacSysExt-QA/ZTMesh.pkg
        else [ "\$repository_name" = 'ztmesh-windows' ]
            export package_path=Release/ZTMesh_Installer.exe
        fi
        ;;
    Demo)
	    if [ "\$repository_name" = 'ztmesh-apple' ]; then
            export package_path=ZTMeshMacSysExt-Demo/ZTMesh.pkg
        else [ "\$repository_name" = 'ztmesh-windows' ]
            export package_path=Release-Demo/ZTMesh_Installer.exe
        fi
        ;;
    Prod)
       if [ "\$repository_name" = 'ztmesh-apple' ]; then
            export package_path=ZTMeshMacSysExt/ZTMesh.pkg
        else [ "\$repository_name" = 'ztmesh-windows' ]
            export package_path=Release/ZTMesh_Installer.exe
        fi
        ;;
esac

repo_url=\$(echo https://repo.optm.network/repository/ztmesh-client-"\$repository_name"/"\$release_tag"/"\$package_path")
echo "\$repo_url"

curl -X 'GET' -u "\$repoid":"\$repotokenpw" -O "\$repo_url" -H 'accept: application/json'
ls -lah

if [ "\$repository_name" = 'ztmesh-apple' ]; then
	client_file=ZTMesh.pkg
else
	client_file=ZTMesh_Installer.exe
fi
echo "\$client_file"

s3_url=\$(echo s3://resources-"\$ENVIRONMENT".cyberight.net/"\$repository_name"/"\$release_tag"/)
aws s3 cp "\$client_file" "\$s3_url"

version_file='version.txt'
echo "\$repository_name"-"\$release_tag" > "\$version_file"

aws s3 cp "\$version_file" "\$s3_url"

            '''
        }
    }
  }
}