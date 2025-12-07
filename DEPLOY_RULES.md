# Hướng dẫn Deploy Firebase Rules

## Bước 1: Deploy Firestore Rules

1. Vào Firebase Console: https://console.firebase.google.com
2. Chọn dự án của bạn
3. Vào **Firestore Database** → **Rules** tab
4. Copy nội dung từ file `firestore.rules` (tuyệt đối toàn bộ)
5. Paste vào editor trong Firebase Console
6. Click **Publish**

## Bước 2: Deploy Realtime Database Rules

1. Vào Firebase Console
2. Chọn dự án của bạn  
3. Vào **Realtime Database** → **Rules** tab
4. Copy nội dung từ file `database.rules.json` (toàn bộ)
5. Paste vào editor trong Firebase Console
6. Click **Publish**

## Lưu ý quan trọng

- **Luôn backup** rules trước khi thay đổi
- Nếu sai, tất cả các operation sẽ fail với PERMISSION_DENIED
- Kiểm tra logs tại **Firestore** → **Logs** hoặc **Realtime Database** → **Logs** để debug

## Tài khoản Test Admin

Sau khi deploy xong:
- Email: `admin@gmail.com`
- Mật khẩu: `123456`
- Role: `admin`

Click nút "Tạo tài khoản admin mẫu" ở login screen để tạo

