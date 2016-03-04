# EpiDial

## Securité

La sécurisation de la communication se fait à l'aide de 2 algorithmes de chiffrement: RSA et AES.

### RSA

RSA est un algorithme de chiffrement asymétrique (la clé de chiffrement et la clé de déchiffrement sont différentes, et on ne peut pas déduire la clé de déchiffrement en partant de la clé de chiffrement).

La clé publique est donc la clé de chiffrement, et la clé privée est la clé de déchiffrement. Tu peux donner la clé publique à n'importe qui, la seule chose qu'il pourra faire c'est encrypter un message.

Le problème de l'implémentation de RSA par Java (...), c'est qu'il ne peut pas chiffrer un message de plus de 117 octets (...). Il faut donc communiquer avec un autre algorithme.

### AES

AES est un algorithme de chiffrement symétrique (il n'y a qu'une seule pour chiffrer et déchiffrer). Donc donner la clé en clair revient à communiquer en clair.

### Sur EpiDial

Le serveur crée une paire de clés RSA unique et fourni une route pour la récupérer (en clair): `GET /server_key`.

Le client crée une clé AES au lancement de l'application (random), et demande la clé RSA publique du serveur.
Lorsque le client se connecte à l'application (sign in / sign up), il envoie toutes ses informations chiffrées avec RSA (nick / email / password / clé AES):  `GET /sign_in` et `GET /sign_up`.

Toutes les données chiffrées sont ensuite encodées en base64 (histoire d'avoir une string pour pouvoir le mettre dans un json).

Ex:
```
{
	nickname: "nickname_rsa_base64",
	email: "email_rsa_base64",
	password: "password_rsa_base64",
	key: "aes_key_rsa_base64"
}
```

Le serveur lui répond en lui donnant son *user_id* et son *authentication_token* (réponse chiffrée avec la clé AES du client fournie dans la requête).

Après l'authentification du client, toutes les communications se font sur la route `GET /secure_route/:user_id`, et la véritable route recherchée est dans la requête. Tout le body est chiffré avec AES puis base64 (le serveur peut récupérer la clé grâce à l'user_id précisé dans la route).

Ex:
```
{
	method: "send_message",
	body: {
		content: "content",
		contact: contactId,
		authToken: "authToken"
	}
}
```
