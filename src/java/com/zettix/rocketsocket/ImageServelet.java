/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.zettix.rocketsocket;

import com.zettix.terrain.DataBaseHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author sean
 */
@WebServlet(name = "ImageServelet", urlPatterns = {"/ImageServelet"})
public class ImageServelet extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    
    private int requestCount = 0;
    private DataBaseHandler db = null;
    
    public void SetDb(DataBaseHandler dbin) {
        System.out.println("Setting DB for Image Server");
        db = dbin;
    }
    
    
    private void ConnectToDataBase() {
        db = new DataBaseHandler("/var/tmp/smallmars/smallmarsimages.db");
        db.Connect();
    }
    
    
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        if (db == null) {
            ConnectToDataBase();
        }
        String query = request.getQueryString();
        String val = "No Image.";
        if (query != null && query.contains("image=") && query.length() > 10) {
             //response.setContentType("image/jpeg");
             val = query.substring(query.indexOf("image=") + 6);
             if (db != null) {
                 byte[] result = db.getBlob("images", val);
                 int isize = result.length;
                 if (isize > 0) {
                     response.setContentType("image/jpeg");
                     response.setContentLength(isize);
                     //try {
                        OutputStream out = response.getOutputStream();
                        out.write(result);
                        return;
                     
                 } else {
                     val = "Zero Bytes";
                 }
             } else {
                 val = "Database not connected.";
             }
        }
        
        
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            /* TODO output your page here. You may use following sample code. */
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet ImageServelet</title>");            
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet ImageServelet at " + request.getContextPath() + "</h1>");
            out.println("Been saying this ");
            out.println(requestCount++);
            out.println(" times... val=");
            out.println(val);
            out.println(" for ");
            out.println(request.getQueryString());
            if (db != null) {
                out.println("<p> DATABASE NOT CONNECTED.");
            } else {
                out.println("<p> Connect to Database");
            }
            out.println("</body>");
            out.println("</html>");
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // processRequest(request, response);
        System.out.println("NO POSTING!!!!!!!!  IMAGE SERVER NO LIKE.");
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Image Server via DataBase";
    }// </editor-fold>

}
