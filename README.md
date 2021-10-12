## What it does

- The code simulates 2 legs of a round trip to the server
- c2s ( client to server ) where client encrypts and server decrypts 
- s2c ( server to client ) where server encrypts and client decrypts

## To Build 

     mvn clean install

## To Run the client to server leg

- cd to the target directory
- touch a file by running echo "abcd" > test
- Simulate the client to server leg by running
```
  java -jar payload-encryption-decryption-1.0-SNAPSHOT-jar-with-dependencies.jar c2s test
```

## To Run the server to client leg

- cd to the target directory
- touch a file by running echo "abcd" > test
- Simulate the server to client leg by running
```
    java -jar payload-encryption-decryption-1.0-SNAPSHOT-jar-with-dependencies.jar s2c test
```  
## Keys
As you can see from the Keys.java class there are 4 keys that are involved in this cryptography
process, 2 of them on each leg. The server uses

- Client's public key ( to verify signature )
- Server's private key ( to decrypt message )

The client uses

- Server's public key ( to verify signature )
- Client's private key ( to decrypt message )

## Creating the key pair
We leveraged 
- open ssl  
- java keytool 
as the tools to crete these pairs. The client and the  server will have to go through this exercise.

## Step 1 Creation of RSA keys

Create a keystore using java keytool. The command to do this is

    keytool -genkey -alias ApiHubUATcertalias -keyalg RSA -keystore ApiHubUATkeystorejks  -keysize 2048

Change the alias to reflect a name of choice, this will prompt you for a password , enter a password of choice

The keytool will then ask you to fill out a series of questions (answer appropriately)

Prompt        | Sample Value  | Description
------------- | ------------- | -----------
Enter a password | abcd1234 | Enter password that meets requirements
What is your first and last name   | What is your first and last name | Usually the url of your company
What is the name of your organizational unit?   | Mobile | Mobile
What is the name of your organization | Mobile | org name
City name | coolville | city
State | TX | State code
Country code | US | country

This should create a keystore in the directory where you ran this command. Ensure that you see the file


## Step 2 Convert format 

The first step is Changing format of the keystore created into pkcs format. User the below command for it

    keytool -importkeystore -srckeystore ApiHubUATkeystorejks -destkeystore ApiHubUATkeystorejks -deststoretype pkcs12

This should change the file to pkcs12 format. It also backs up the keystore with an .old extension

## Step 3 Extract private key and public certificate

Extract the private key from this file by running the following command

        openssl pkcs12 -in  ApiHubUATkeystorejks -nodes -nocerts -out privatekey

Now we can extract the public certificate   from this file by running the following command

        openssl pkcs12 -in ApiHubUATkeystorejks -nokeys -out public-cert-file


You should now see the private key and the public certificate in your directory

## Optionally Getting the certificates signed

Optionally, the certificates can be signed by a trusted authority. If you want the certificate to be signed, 
create a csr (certificate signing request) by running the following command

    keytool -certreq -alias ApiHubUATcertalias -keystore ApiHubUATkeystorejks -file certreq_uat.csr


## Upload the csr file to your trusted authority’s portal. When the authority signs it, you will get back the certificate along with the signers. Import certificate and all the signers back into the keystore by running the following commands. Please use the same keystore that was used to generate the keys

    keytool -import -trustcacerts -alias signer_root   Root.crt -keystore ApiHubUATkeystorejks

    keytool -import -trustcacerts -alias signer_inter   inter.crt -keystore ApiHubUATkeystorejks

    keytool -import -trustcacerts -alias signer_srvr   srvr.crt -keystore ApiHubUATkeystorejks


## Extracts certificates
To extract the public cert and private key from the certificate returned by the trusted authority, run the following commands

### Extract private key from the p12 file
        openssl pkcs12 -in ApiHubUATkeystorejks.p12 -clcerts -nokeys -out public-cert-file

### Extract private key from the p12 file
        openssl pkcs12 -info -in ApiHubUATkeystorejks.p12 -nodes -nocerts -out private.key



