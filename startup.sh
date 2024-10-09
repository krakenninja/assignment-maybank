#!/bin/sh
SCRIPT_HOME="$(dirname "$(realpath "${BASH_SOURCE[0]}")")"

fn_build_project()
{
    mvn -B clean install package -DskipTests jib:dockerBuild -f $SCRIPT_HOME/pom.xml
    mvnCleanInstallJibRc=$?
    if [ "$mvnCleanInstallJibRc" != "0" ]; then
        echo "ERROR : Maven command 'mvn -B clean install -DskipTests jib:dockerBuild -f $SCRIPT_HOME/pom.xml' FAILURE ($mvnCleanInstallJibRc)"
        exit $mvnCleanInstallJibRc
    fi
}

fn_start_microservices()
{
    fn_build_project
    fnBuildProjectRc=$?
    if [ "$fnBuildProjectRc" != "0" ]; then
        exit $fnBuildProjectRc
    fi
    
    docker compose --env-file $SCRIPT_HOME/.env -f $SCRIPT_HOME/resources/docker/docker-compose.yml up -d
    dockerComposeUpRc=$?
    if [ "$dockerComposeUpRc" != "0" ]; then
        echo "ERROR : Docker command 'docker compose -f $SCRIPT_HOME/resources/docker/docker-compose.yml up' FAILURE ($dockerComposeUpRc)"
        exit $dockerComposeUpRc
    fi
}

source $SCRIPT_HOME/.env

fn_start_microservices
