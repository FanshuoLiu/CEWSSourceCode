package com.encryptrdSoftware.hnust.controller;
import com.encryptrdSoftware.hnust.util.ZipExtractorUtils;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FileExistsException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

@WebServlet("/Upload")
public class UploadServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final String UPLOAD_DIRECTORY = "uploads"; // 文件上传目录

    public static String filePath;
    public static String Path="your upload path\\uploads";

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");

        // 检查是否为文件上传请求
        if (ServletFileUpload.isMultipartContent(req)) {
            DiskFileItemFactory factory = new DiskFileItemFactory();
            ServletFileUpload upload = new ServletFileUpload(factory);
            try {
                List<FileItem> fileItems = upload.parseRequest(req);
                for (FileItem item : fileItems) {
                    if (!item.isFormField()) {
                        String fileName = new File(item.getName()).getName();
                        filePath = getServletContext().getRealPath("") + File.separator + UPLOAD_DIRECTORY + File.separator+ fileName;
                        System.out.println("文件路径:"+filePath);
                        File uploadDir = new File(getServletContext().getRealPath("") + File.separator + UPLOAD_DIRECTORY);
                        if (!uploadDir.exists()) {
                            uploadDir.mkdir();
                        }

                        File uploadedFile = new File(filePath);
                        item.write(uploadedFile);
                        if (fileName.endsWith(".zip")){
                            ZipExtractorUtils.unzip(filePath, Path);
                        }

                        resp.getWriter().write("{\"status\":\"success\",\"message\":\"文件上传成功: " + fileName + "\"}");
                        return;
                    }
                }
            } catch (FileUploadException e) {
                e.printStackTrace();
                resp.getWriter().write("{\"status\":\"error\",\"message\":\"文件上传失败: " + e.getMessage() + "\"}");
            } catch (FileExistsException e){
                e.printStackTrace();
                resp.getWriter().write("{\"status\":\"error\",\"message\":\"文件已存在\"}");
            }catch (IOException e) {
                e.printStackTrace();
                resp.getWriter().write("{\"status\":\"error\",\"message\":\"" + e.getMessage() + "\"}");
            }catch (IllegalArgumentException e){
                 e.printStackTrace();
                resp.getWriter().write("{\"status\":\"error\",\"message\":\"" + e.getMessage() + "\"}");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            resp.getWriter().write("{\"status\":\"error\",\"message\":\"请求不包含文件\"}");
        }
    }
    public static String getPath() {
        return Path;
    }
     public static boolean isBmpOneBitDepth(String filePath) throws IOException {
        try (FileInputStream fis = new FileInputStream(filePath)) {
            byte[] header = new byte[54];
            if (fis.read(header) != header.length) {
                throw new IOException("无法读取完整的BMP头部");
            }

            // BMP文件的位深度在第28到29字节（偏移为28）
            int bitDepth = ((header[28] & 0xFF) | (header[29] & 0xFF) << 8);
            return bitDepth == 1;
        }
    }

}
