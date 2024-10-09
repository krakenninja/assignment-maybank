#!/bin/sh

SCRIPT_HOME="$(dirname "$(realpath "${BASH_SOURCE[0]}")")"

fn_stop_microservices()
{
    docker compose --env-file $SCRIPT_HOME/.env -f $SCRIPT_HOME/resources/docker/docker-compose.yml down
    dockerComposeDownRc=$?
    if [ "$dockerComposeDownRc" != "0" ]; then
        echo "ERROR : Docker command 'docker compose -f $SCRIPT_HOME/resources/docker/docker-compose.yml down' FAILURE ($dockerComposeDownRc)"
        exit $dockerComposeDownRc
    fi
}

fn_remove_images()
{
	userManagementServiceImages=$(docker images --format "{{.Repository}}:{{.Tag}}" | grep "user-management-service")
	if [ ! -z "$userManagementServiceImages" ]; then
		docker rmi -f $userManagementServiceImages
	fi
	
	accountingManagementServiceImages=$(docker images --format "{{.Repository}}:{{.Tag}}" | grep "accounting-management-service")
	if [ ! -z "$accountingManagementServiceImages" ]; then
		docker rmi -f $accountingManagementServiceImages
	fi
}

fn_remove_rds_data()
{
	if [ -d $RDS_USER_SERVICE_VOLUME_DATA_PATH ]; then
		rm -rf $RDS_USER_SERVICE_VOLUME_DATA_PATH
	fi
	if [ -d $RDS_ACCOUNTING_SERVICE_VOLUME_DATA_PATH ]; then
		rm -rf $RDS_ACCOUNTING_SERVICE_VOLUME_DATA_PATH
	fi
}

fn_remove_uploads()
{
	if [ -d $ACCOUNTING_MANAGEMENT_SERVICE_VOLUME_UPLOADS_PATH ]; then
		rm -rf $ACCOUNTING_MANAGEMENT_SERVICE_VOLUME_UPLOADS_PATH
	fi
}

sh $SCRIPT_HOME/shutdown.sh

source $SCRIPT_HOME/.env
fn_stop_microservices
fn_remove_images
fn_remove_rds_data
fn_remove_uploads