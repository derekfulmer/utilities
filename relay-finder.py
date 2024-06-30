#!/usr/bin python3

import boto3
import json


def get_relay_public_ips():
    """
    Get the public IP addresses from S3 using relay.json as source of truth for
    all current ZTRelay hosts and their network information."""
    Bucket = 'resources-dev.cyberight.net'
    Key = 'relay.json'
    s3 = boto3.client('s3')

    obj = s3.get_object(Bucket=bucket, Key=key)
    j = json.loads(obj['Body'].read())


        

def get_route53_relay_records():
    """
    Read records from Route53, compare them to relay public IPs,
    delete if not a match."""
    r53client = boto3.client("route53")

    hosted_zone = get_hosted_zone_id(fargs)
    name = f"{fargs.name}.{fargs.zone_name}."
    response = r53.list_resource_record_sets(
        HostedZoneId=hosted_zone,
        StartRecordName=name,
        StartRecordType=fargs.type,
        MaxItems="1",
    )

    #response contains FQDN of resource record sets. Parse this via a regex to find records in the response with 'relay-123-456'?

# Get relay json
#iterate through json and compare to route 53 list hostname match to json, if not get rid of route 53 record and upsert back into route 53
