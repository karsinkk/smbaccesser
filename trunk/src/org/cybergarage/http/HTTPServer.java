/******************************************************************
 *
 *  CyberHTTP for Java
 *
 *  Copyright (C) Satoshi Konno 2002-2003
 *
 *  File: HTTPServer.java
 *
 *  Revision;
 *
 *  12/12/02
 *    - first revision.
 *  10/20/03
 *    - Improved the HTTP server using multithreading.
 *  08/27/04
 *    - Changed accept() to set a default timeout, HTTP.DEFAULT_TIMEOUT, to the socket.
 *
 ******************************************************************/

package org.cybergarage.http;

import org.cybergarage.util.Debug;
import org.cybergarage.util.ListenerList;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;


public class HTTPServer implements Runnable
{
    private final static String tag = "HTTPServer";
    // //////////////////////////////////////////////
    // Constants
    // //////////////////////////////////////////////

    public final static String NAME = "CyberHTTP";
    public final static String VERSION = "1.0";

    public final static int DEFAULT_PORT = 80;

    public final static int DEFAULT_TIMEOUT = 15 * 1000;

    public static String getName()
    {

        String osName = System.getProperty("os.name");

        String osVer = System.getProperty("os.version");
        return osName + "/" + osVer + " " + NAME + "/" + VERSION;
    }

    // //////////////////////////////////////////////
    // Constructor
    // //////////////////////////////////////////////

    public HTTPServer()
    {
        serverSock = null;

    }

    // //////////////////////////////////////////////
    // ServerSocket
    // //////////////////////////////////////////////


    private ServerSocket serverSock = null;

    private InetAddress bindAddr = null;

    private int bindPort = 0;

    protected int timeout = DEFAULT_TIMEOUT;

    public ServerSocket getServerSock()
    {
        return serverSock;
    }


    public String getBindAddress()
    {
        if (bindAddr == null)
        {
            return "";
        }
        return bindAddr.getHostAddress();
    }

    public int getBindPort()
    {
        return bindPort;
    }

    // //////////////////////////////////////////////
    // open/close
    // //////////////////////////////////////////////


    public synchronized int getTimeout()
    {
        return timeout;
    }


    public synchronized void setTimeout(int timeout)
    {
        this.timeout = timeout;
    }


    public boolean open(InetAddress addr, int port)
    {
        if (serverSock != null)
            return true;
        try
        {
            serverSock = new ServerSocket(bindPort, 0, bindAddr);


        }
        catch (IOException e)
        {
            return false;
        }
        return true;
    }


    public boolean open(String addr, int port)
    {
        if (serverSock != null)
        {
            return true;
        }
        try
        {
            bindAddr = InetAddress.getByName(addr);
            bindPort = port;
            serverSock = new ServerSocket(bindPort, 0, bindAddr);



        }
        catch (IOException e)
        {
            return false;
        }
        return true;
    }


    public boolean close()
    {
        if (serverSock == null)
        {
            return true;
        }
        try
        {
            serverSock.close();
            serverSock = null;
            bindAddr = null;
            bindPort = 0;

        }
        catch (Exception e)
        {
            Debug.warning(e);
            return false;
        }
        return true;
    }


    public Socket accept()
    {
        if (serverSock == null)
            return null;
        try
        {
            Socket sock = serverSock.accept();
            sock.setSoTimeout(getTimeout());
            return sock;
        }
        catch (Exception e)
        {
            return null;
        }
    }

    public boolean isOpened()
    {
        return (serverSock != null) ? true : false;
    }

    // //////////////////////////////////////////////
    // httpRequest
    // //////////////////////////////////////////////


    private final ListenerList httpRequestListenerList = new ListenerList();


    public void addRequestListener(HTTPRequestListener listener)
    {
        httpRequestListenerList.add(listener);
    }

    public void removeRequestListener(HTTPRequestListener listener)
    {
        httpRequestListenerList.remove(listener);
    }

    public void performRequestListener(HTTPRequest httpReq)
    {
        int listenerSize = httpRequestListenerList.size();
        for (int n = 0; n < listenerSize; n++)
        {
            HTTPRequestListener listener = (HTTPRequestListener) httpRequestListenerList
                    .get(n);


            listener.httpRequestRecieved(httpReq);
        }
    }

    // //////////////////////////////////////////////
    // run
    // //////////////////////////////////////////////

    private Thread httpServerThread = null;

    @Override
    public void run()
    {

        if (isOpened() == false)
        {
            return;
        }


        Thread thisThread = Thread.currentThread();

        while (httpServerThread == thisThread)
        {
            // �߳��ò����ص�׼������״̬
            Thread.yield();
            Socket sock;
            try
            {
                Debug.message("accept ...");

                sock = accept();
                if (sock != null)
                {
                    Debug.message("sock = " + sock.getRemoteSocketAddress());
                }
            }
            catch (Exception e)
            {
                break;
            }


            HTTPServerThread httpServThread = new HTTPServerThread(this, sock);
            httpServThread.start();
            Debug.message("httpServThread ...");
        }
    }

    public boolean start()
    {
        StringBuffer name = new StringBuffer("Cyber.HTTPServer/");
        name.append(serverSock.getLocalSocketAddress());
        httpServerThread = new Thread(this, name.toString());
        httpServerThread.start();
        return true;
    }

    public boolean stop()
    {
        httpServerThread = null;
        return true;
    }
}
