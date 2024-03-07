job('DotnetWebAPI/compile'){ 	
	description 'Compile application'
	label('Windows')
	scm {
        	github('swagner7764/DotnetWebAPI', 'master')
    	}
  	steps{
      		msBuild {
	            	msBuildInstallation('MSBuild 2022')
	            	buildFile('${WORKSPACE}/DotnetWebAPI.sln')	
                    args('-restore')
        	}	
            
   	}    
  	publishers {
        	downstream 'DotnetWebAPI/containerize', 'SUCCESS'
   	}
}

job('DotnetWebAPI/containerize'){
  	description 'Dockerize application'
	customWorkspace('C:/tools/jenkins-agent/workspace/DotnetWebAPI/compile')
	label('Windows')
    	steps{
		powerShell 'docker build . -t dotnetwebapi:1.0.2.$ENV:BUILD_NUMBER -t dotnetwebapi:latest -f C:/tools/jenkins-agent/workspace/DotnetConsoleApp/compile/DotnetWebAPI/DockerFile'
		powerShell 'aws ecr get-login-password --region us-west-1 | docker login --username AWS --password-stdin 105414332808.dkr.ecr.us-west-1.amazonaws.com'
		powerShell 'docker tag dotnetwebapi:1.0.2.$ENV:BUILD_NUMBER 105414332808.dkr.ecr.us-west-1.amazonaws.com/dotnetwebapi:1.0.2.$ENV:BUILD_NUMBER'
		powerShell 'docker push 105414332808.dkr.ecr.us-west-1.amazonaws.com/dotnetwebapi:1.0.2.$ENV:BUILD_NUMBER'
		powerShell 'docker tag dotnetwebapi:1.0.2.$ENV:BUILD_NUMBER 105414332808.dkr.ecr.us-west-1.amazonaws.com/dotnetwebapi:latest'
		powerShell 'docker push 105414332808.dkr.ecr.us-west-1.amazonaws.com/dotnetwebapi:latest'
	}  	
}

deliveryPipelineView('DotnetConsoleApp/dotnet delivery pipeline') {
    showAggregatedPipeline true
    enableManualTriggers true
    pipelineInstances 5
    pipelines {
        component('DotnetWebAPI/dotnet delivery pipeline', 'DotnetWebAPI/compile')
    }
}