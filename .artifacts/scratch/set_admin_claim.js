/**
 * Node.js script to set the admin custom claim for a Firebase Auth user.
 *
 * Prerequisites:
 * 1. Install firebase-admin: npm install firebase-admin
 * 2. Download your service account key from Firebase Console -> Project Settings -> Service Accounts -> Generate new private key.
 * 3. Save it as `serviceAccountKey.json` in the same directory.
 * 4. Run: node set_admin_claim.js <USER_UID>
 */

const admin = require('firebase-admin');
const serviceAccount = require('./serviceAccountKey.json');

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount)
});

const uid = process.argv[2];

if (!uid) {
  console.error('Please provide the user UID as an argument.');
  console.error('Usage: node set_admin_claim.js <USER_UID>');
  process.exit(1);
}

admin.auth().setCustomUserClaims(uid, { admin: true })
  .then(() => {
    console.log(`Successfully set admin claim (admin: true) for user UID: ${uid}`);
    process.exit(0);
  })
  .catch((error) => {
    console.error('Error setting custom user claim:', error);
    process.exit(1);
  });
