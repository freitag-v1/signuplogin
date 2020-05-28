package swcapstone.freitag.springsecurityjpa.api;

import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;

import org.springframework.stereotype.Service;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class ObjectStorageApiClient {

    private static final String REGION_NAME = "kr-standard";
    private static final String ENDPOINT = "https://kr.object.ncloudstorage.com";

    private static final String ACCESS_KEY = "sQG5BeaHcnvvqK4FI01A";
    private static final String SECRET_KEY = "mvNVjSac240XvnrK4qF39HpoMvvtMQMzUnnNHaRV";


    // S3 client
    final AmazonS3 s3 = AmazonS3ClientBuilder.standard()
            .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(ENDPOINT, REGION_NAME))
            .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(ACCESS_KEY, SECRET_KEY)))
            .build();


    public String putObject(String bucketName, File uploadFile) throws Exception {

        String objectName = uploadFile.getName();

        try {
            /*PutObjectResult putObjectResult = */s3.putObject(bucketName, objectName, uploadFile);
            System.out.format("Object %s has been created.\n", objectName);

            return objectName;

        } catch (AmazonS3Exception e) {
            e.printStackTrace();
        } catch (SdkClientException e) {
            e.printStackTrace();
        }

        return null;
    }


    public boolean putBucket(String bucketName) {

        if (s3.doesBucketExistV2(bucketName)) {
            System.out.format("Bucket %s already exists.\n", bucketName);
            return false;
        }

        s3.createBucket(bucketName);
        setBucketCORS(bucketName);

        System.out.format("Bucket %s has been created.\n", bucketName);
        return true;

    }

    public List<String> listObjects(String bucketName) {

        List<String> objectNameList = new ArrayList<>();

        ListObjectsRequest listObjectsRequest = new ListObjectsRequest()
                .withBucketName(bucketName)
                .withMaxKeys(300);
        ObjectListing objectListing = s3.listObjects(listObjectsRequest);

        for(S3ObjectSummary s : objectListing.getObjectSummaries()) {
            String objectName = s.getKey();
            objectNameList.add(objectName);
        }

        return objectNameList;
    }

    public boolean objectExists(String bucketName, String objectName) {

        List<String> objectNameList = listObjects(bucketName);

        if (objectNameList.contains(objectName))
            return true;

        System.out.println(bucketName + " 에 " + objectName + " 없음! ");
        return false;
    }

    private void setBucketCORS(String bucketName) {

        List<CORSRule.AllowedMethods> ruleAM = new ArrayList<CORSRule.AllowedMethods>();
        ruleAM.add(CORSRule.AllowedMethods.GET);
        CORSRule rule = new CORSRule().withId("CORSRule").withAllowedMethods(ruleAM)
                .withAllowedOrigins(Arrays.asList("*"));

        List<CORSRule> rules = new ArrayList<CORSRule>();
        rules.add(rule);

        // Add the rules to a new CORS configuration.
        BucketCrossOriginConfiguration configuration = new BucketCrossOriginConfiguration();
        configuration.setRules(rules);

        // Add the configuration to the bucket.
        s3.setBucketCrossOriginConfiguration(bucketName, configuration);

        // end
    }
}



