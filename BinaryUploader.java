package com.zlfinfo.common.ueditor.upload;

import com.zlfinfo.common.ueditor.PathFormat;
import com.zlfinfo.common.ueditor.define.AppInfo;
import com.zlfinfo.common.ueditor.define.BaseState;
import com.zlfinfo.common.ueditor.define.FileType;
import com.zlfinfo.common.ueditor.define.State;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;


public class BinaryUploader {
    static Logger logger = LoggerFactory.getLogger(BinaryUploader.class);
    
	public static final State save(HttpServletRequest request, Map<String, Object> conf) {
		
		boolean isAjaxUpload = request.getHeader( "X_Requested_With" ) != null;

		if (!ServletFileUpload.isMultipartContent(request)) {
			return new BaseState(false, AppInfo.NOT_MULTIPART_CONTENT);
		}
		
		ServletFileUpload upload = new ServletFileUpload(new DiskFileItemFactory());

        if ( isAjaxUpload ) {
            upload.setHeaderEncoding( "UTF-8" );
        }
//		System.out.println(request.getLocalAddr());
		try {
			MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
			MultipartFile file = multipartRequest.getFile("upfile");

//			System.out.println(file.getOriginalFilename()+"====================");
			/*物理存储路径和映射显示路径*/
			String filedir = PropertiesLoaderUtils.loadAllProperties("application-uploadprop.yml").getProperty("filedir");
			String spath = PropertiesLoaderUtils.loadAllProperties("application-uploadprop.yml").getProperty("spath");
			String fileName = file.getOriginalFilename();  // 文件名
			String suffixName = fileName.substring(fileName.lastIndexOf("."));  // 后缀名
			/*动态生成图片的路经*/
			String contextpath = "/ueditor/" + UUID.randomUUID().toString().replace("-", "")+ suffixName;
			/*创建接口服务存储文件的目录*/
			File localFile = new File(filedir + spath + "/ueditor");
			if (!localFile.exists()) {
				localFile.mkdirs();
			}
			filedir = filedir + spath +  contextpath;
			file.transferTo(new File((filedir)));
			/*需要自动生成上下文的路径*/
//			String imgpath=spath+contextpath;
			String path = request.getScheme()+"://"+request.getLocalAddr()+":"+request.getServerPort()+ spath + spath + contextpath;
			State state = new BaseState();
			state.putInfo( "size", file.getSize() );
//			state.putInfo( "title", path);//文件名填入此处
			state.putInfo("url", path);
//			state.putInfo( "group", "");//所属group填入此处
//			state.putInfo( "url", "");//文件访问的url填入此处
			return state;

//			String savePath = (String) conf.get("savePath");
//			String localSavePathPrefix = (String) conf.get("localSavePathPrefix");
//			String originFileName = file.getOriginalFilename();
//			String suffix = FileType.getSuffixByFilename(originFileName);
//
//			System.out.println(suffix + " 1+++++++++++++++++++++");
//
//			originFileName = originFileName.substring(0, originFileName.length() - suffix.length());
//
//			System.out.println(originFileName + " 2+++++++++++++++++++++");
//
//			savePath = savePath + suffix;
//
//			long maxSize = ((Long) conf.get("maxSize")).longValue();
//
//			if (!validType(suffix, (String[]) conf.get("allowFiles"))) {
//				return new BaseState(false, AppInfo.NOT_ALLOW_FILE_TYPE);
//			}
//			savePath = PathFormat.parse(savePath, originFileName);
//			localSavePathPrefix = localSavePathPrefix + savePath;
//			String physicalPath = localSavePathPrefix;
//			logger.info("BinaryUploader physicalPath:{},savePath:{}",localSavePathPrefix,savePath);
//			InputStream is = file.getInputStream();
//
//			//在此处调用ftp的上传图片的方法将图片上传到文件服务器
//			String path = physicalPath.substring(0, physicalPath.lastIndexOf("/"));
//			String picName = physicalPath.substring(physicalPath.lastIndexOf("/")+1, physicalPath.length());
//			State storageState = StorageManager.saveFileByInputStream(request, is, path, picName, maxSize);
//
//			is.close();
//			State storageState = StorageManager.saveFileByInputStream(request,file);
//			return storageState;
		} catch (Exception e) {
			return new BaseState(false, AppInfo.PARSE_REQUEST_ERROR);
		}
	}

	private static boolean validType(String type, String[] allowTypes) {
		List<String> list = Arrays.asList(allowTypes);

		return list.contains(type);
	}
}
