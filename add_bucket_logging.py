#!/usr/bin/env python3

import argparse
import boto3
from collections import namedtuple


s3 = boto3.client("s3")


def arg_parse():
    """
    Defines arguments for user input
    """
    parser = argparse.ArgumentParser(
        prog="add_bucket_logging",
        description="Adds bucket logging configuration to AWS S3 buckets.",
    )
    parser.add_argument(
        "--env",
        action="store",
        dest="env",
        help="The AWS account/environment wherein buckets reside.",
        type=str,
        required=True,
    )

    args = parser.parse_args()
    return args


def get_s3_buckets():
    """Queries S3 API to get bucket names and dumps them into a list based on their regional location."""
    buckets = s3.list_buckets()
    bucket_names = buckets["Buckets"]
    Results = namedtuple(
        "Results",
        ["us_east1_bucket_list", "us_east2_bucket_list", "us_west2_bucket_list"],
    )

    us_east1_bucket_list = []
    us_east2_bucket_list = []
    us_west2_bucket_list = []
    for bucket in bucket_names:
        if (
            s3.head_bucket(Bucket=bucket["Name"])["ResponseMetadata"]["HTTPHeaders"][
                "x-amz-bucket-region"
            ]
            == "us-east-1"
        ):
            us_east1_bucket_list.append(bucket["Name"])
        elif (
            s3.head_bucket(Bucket=bucket["Name"])["ResponseMetadata"]["HTTPHeaders"][
                "x-amz-bucket-region"
            ]
            == "us-west-2"
        ):
            us_west2_bucket_list.append(bucket["Name"])
        else:
            us_east2_bucket_list.append(bucket["Name"])

    return Results(us_east1_bucket_list, us_east2_bucket_list, us_west2_bucket_list)


def add_bucket_logging(fargs=None):
    """Sets up bucket logging to a specific bucket in a specific region.
    If Bucket X is in us-east-1, its logs cannot be shipped to Log Bucket Y in us-east-2.
    """
    bucket_list = get_s3_buckets()

    log_bucket_east1 = f"cyberight-{fargs.env}-s3logs-east1"
    log_bucket_east2 = f"cyberight-{fargs.env}-s3logs-east2"
    log_bucket_west2 = f"cyberight-{fargs.env}-s3logs-west2"

    for i in bucket_list.us_east1_bucket_list:
        response = s3.put_bucket_logging(
            Bucket=i,
            BucketLoggingStatus={
                "LoggingEnabled": {
                    "TargetBucket": log_bucket_east1,
                    "TargetPrefix": f"{i}/logs/",
                }
            },
        )

    for i in bucket_list.us_east2_bucket_list:
        response = s3.put_bucket_logging(
            Bucket=i,
            BucketLoggingStatus={
                "LoggingEnabled": {
                    "TargetBucket": log_bucket_east2,
                    "TargetPrefix": f"{i}/logs/",
                }
            },
        )

    for i in bucket_list.us_west2_bucket_list:
        response = s3.put_bucket_logging(
            Bucket=i,
            BucketLoggingStatus={
                "LoggingEnabled": {
                    "TargetBucket": log_bucket_west2,
                    "TargetPrefix": f"{i}/logs/",
                }
            },
        )


def main():
    args = arg_parse()
    add_bucket_logging(fargs=args)


if __name__ == "__main__":
    main()
