package com.eventostec.api.adapters.outbound.storage;

import java.io.File;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.s3.AmazonS3;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ImageUploaderAdapter {

	  @Value("${aws.bucket.name}")
	  private String bucketName;	
	  
	  @Autowired
	  private AmazonS3 s3Client;
	  
	  private String uploadImg(MultipartFile multipartFile) {
	        String filename = UUID.randomUUID() + "-" + multipartFile.getOriginalFilename();

	        try {
	            // Crie um arquivo temporário
	            File tempFile = File.createTempFile("upload-", filename);
	            multipartFile.transferTo(tempFile);

	            // Faça upload usando o método do SDK v1
	            s3Client.putObject(bucketName, filename, tempFile);

	            // Opcional: construa a URL manualmente ou usando métodos utilitários
	            String fileUrl = s3Client.getUrl(bucketName, filename).toString();

	            // Delete o arquivo temporário
	            tempFile.delete();

	            return fileUrl;
	        } catch (Exception e) {
	        	System.out.println("erro ao subir arquivo"); 
	            return "";
	        }
	    }
}
