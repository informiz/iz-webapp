# Informiz Web Application

### Installation Instructions

### Certificates
Users require crypto-material in order to identify themselves to the blockchain. 
Each organization has (or gets from Informiz) a root-certificate that functions as a 
certificate-authority for its fact-checkers.
You can generate user keys and certificates with `openssl`:

* Save a `client.cnf` file with the following configuration:
```properties
keyUsage = critical, digitalSignature
basicConstraints = critical,CA:FALSE
authorityKeyIdentifier = keyid,issuer
```

* Create a Certificate Signing Request. E.g, for the Admin user of the nasa.org channel:

`openssl req -new -sha256 -nodes -newkey ec:PATH/TO/ca.pem 
-subj "/CN=admin:jane@org.nasa.com:channel-id/C=US/ST=California/L=San Francisco/OU=admin/OU=Economics" 
-keyout admin.nasa.key -out admin.nasa.csr`

* Sign with with the organization's (e.g nasa) certificate-authority:
`openssl x509 -req -sha256 -CA PATH/TO/ca.pem -CAkey PATH/TO/priv_sk -days 730 
-CAcreateserial -CAserial CA.srl -extfile client.cnf -in admin.nasa.csr -out admin.nasa.pem`
 

