#!/bin/sh
SCRIPT_HOME="$(dirname "$(realpath "${BASH_SOURCE[0]}")")"

fn_stop_microservices()
{
    docker compose --env-file $SCRIPT_HOME/.env -f $SCRIPT_HOME/resources/docker/docker-compose.yml stop
    dockerComposeStopRc=$?
    if [ "$dockerComposeStopRc" != "0" ]; then
        echo "ERROR : Docker command 'docker compose -f $SCRIPT_HOME/resources/docker/docker-compose.yml stop' FAILURE ($dockerComposeStopRc)"
        exit $dockerComposeStopRc
    fi
}

source $SCRIPT_HOME/.env

fn_stop_microservices
