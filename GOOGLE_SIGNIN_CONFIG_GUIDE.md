# Google Sign-In Configuration Guide ⚠️

## Current Status:

❌ **Error:** `Unresolved reference: default_web_client_id`

**Reason:** You need to configure Google Sign-In in Firebase Console to get the proper Web Client ID.

---

## How to Fix:

### Step 1: Go to Firebase Console
1. Open [Firebase Console](https://console.firebase.google.com)
2. Select your project: **chatbot-1b843**
3. Go to **Authentication** → **Sign-in method** tab
4. Enable **Google** (if not already enabled)

### Step 2: Create OAuth Client ID
1. Click on **Google** sign-in method
2. Click **Edit configuration**
3. Scroll down to see your OAuth client IDs
4. You should see:
   - Android app OAuth client (for Android)
   - Web OAuth client (what you need)

### Step 3: Get Web Client ID
1. If no Web OAuth client exists, create one:
   - Click "Add another platform" → "Web"
   - Add authorized domains if needed
2. Copy the **Web Client ID** (format: `XXXXXXXXXXXX-XXXXXXXXXXXXXXXXXXXXXXXXXXXXX.apps.googleusercontent.com`)

### Step 4: Update strings.xml
Replace the placeholder in `strings.xml`:

```xml
<!-- BEFORE (placeholder) -->
<string name="default_web_client_id">278992454640-PLACEHOLDER.apps.googleusercontent.com</string>

<!-- AFTER (with real ID from Firebase) -->
<string name="default_web_client_id">YOUR_WEB_CLIENT_ID_HERE</string>
```

### Step 5: Rebuild Project
```bash
./gradlew clean build
```

---

## Alternative: Auto-Generate from google-services.json

If you prefer, Google Play Services can auto-generate this. However, the manual method above is more reliable.

---

## Testing:

After configuration:
1. Run app
2. Click Google login button
3. Should see Google account selection
4. Select account → Grant permissions
5. Auto-login and navigate to MainActivity

---

## Troubleshooting:

**Still getting error after rebuild?**
- Check internet connection
- Invalidate caches in Android Studio: File → Invalidate Caches
- Rebuild project

**Google Sign-In button doesn't work?**
- Verify Web Client ID is correct
- Check Firebase Authentication is enabled
- Ensure internet permission in AndroidManifest.xml

**"Googlelogin failed" toast?**
- Check Web Client ID format (ends with .apps.googleusercontent.com)
- Verify Google account is available on device

---

## Firebase Console Path:

Firebase Console
  → Your Project (chatbot-1b843)
    → Authentication
      → Sign-in method
        → Google (Enable/Configure)
          → Web OAuth Client ID (Copy this!)

---

## Current File Updated:

✅ `strings.xml` - Added `default_web_client_id` string resource  
✅ `Login.kt` - Google Sign-In implementation complete

**Just need to update the placeholder value with your real Web Client ID!**

---

## Need Help?

If you're stuck:
1. Go to Firebase Console
2. Look for "Google" in Authentication → Sign-in method
3. Find the "Web SDK configuration" section
4. Copy the Client ID from there
5. Paste into strings.xml

That's it! 🎉

