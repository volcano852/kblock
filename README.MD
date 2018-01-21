# KBlock

- Toy implementation of bitcoin-like crypto-currency in Spring boot / Kotlin
- The mining is a basic leading zeros hashing proof of work
- The mining difficulty is constant and initialized at the blockchain instantiation
- The mining reward is systematic and constant. The transaction sender cannot leave transaction fee at this stage

## RSA Key CLI

1. Generating key pairs

```shell
ssh-keygen
```

2. Convert PEM private to PKCS8 private
```shell
openssl pkcs8 -topk8 -inform PEM -outform DER -nocrypt -in id_rsa -out id_rsa.p8
```

3. Extract public key from the private key
```shell
openssl rsa -in id-rsa -pubout -outform DER -out id_rsa.p8.pub
```

## Personal working notes

1. What is the current bitcoin leading zeros complexity ?
2. Is adding one zero of complexity really double up the time in bitcoin ? (difficulty -> avg hashes : 2 -> 7,3 -> 15, 4->32, 5->65)
3. my laptop hash capacity: 127960366 hashes in 66 sec. 1_938_793 hash/sec ~ 1.9MHash/sec
4. What is the timestamp protocol used in bitcoin ?
6. Is the minor/contributor put in the block ? Do we keep track of the minor in the hash (and is it being hashed?)
7. Should KBlock always reward with 1.0 ktoken?
