# Windows Setup Instructions

When running the API Gateway on Windows, you might encounter the following error:

```
[WinError 10013] An attempt was made to access a socket in a way forbidden by its access permissions
```

## How to Fix

This error typically occurs because:
1. The port 8000 is already in use by another application
2. You don't have sufficient permissions to bind to ports below 1024
3. Windows Firewall is blocking the connection

### Solution 1: Change the API Gateway Port (Recommended)

Edit the `.env` file in the `api_gateway` directory and change the port:

```
# Change this value to a different port
API_GATEWAY_PORT=8080
```

Good port numbers to try:
- 8080
- 8888
- 9000
- 3000

### Solution 2: Run as Administrator

If you need to use a specific port (like 80 or 443), you'll need to run the application as administrator:

1. Right-click on your command prompt or PowerShell
2. Select "Run as administrator"
3. Navigate to your project directory
4. Run `python run.py`

### Solution 3: Configure Windows Firewall

If your application still can't bind to the port:

1. Press Windows Key + R
2. Type "firewall.cpl" and press Enter
3. Click "Advanced settings"
4. Add a new inbound rule for your application or the specific port

## Checking Ports in Use

To see which ports are already in use on your system:

```
netstat -ano | findstr :8000
```

Replace `8000` with the port number you're trying to use.
