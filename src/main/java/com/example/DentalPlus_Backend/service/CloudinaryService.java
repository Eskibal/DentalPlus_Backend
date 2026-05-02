package com.example.DentalPlus_Backend.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryService {

	private static final long MAX_IMAGE_SIZE_BYTES = 10 * 1024 * 1024;

	private final Cloudinary cloudinary;

	public CloudinaryService(Cloudinary cloudinary) {
		this.cloudinary = cloudinary;
	}

	public String uploadProfileImage(MultipartFile file, Long userId) throws IOException {
		if (!isImageValid(file)) {
			throw new IllegalArgumentException("Invalid profile image");
		}

		Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(),
				ObjectUtils.asMap("folder", "dentalplus/profile-images", "public_id",
						"user_" + userId + "_" + System.currentTimeMillis(), "resource_type", "image", "overwrite",
						false));

		Object secureUrl = uploadResult.get("secure_url");

		if (secureUrl == null) {
			throw new IllegalStateException("Cloudinary did not return an image URL");
		}

		return secureUrl.toString();
	}

	public void deleteImageByUrl(String imageUrl) {
		String publicId = extractPublicIdFromUrl(imageUrl);

		if (publicId == null || publicId.isBlank()) {
			return;
		}

		try {
			cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
		} catch (IOException e) {
			throw new IllegalStateException("Error deleting image from Cloudinary", e);
		}
	}

	public static boolean isImageValid(MultipartFile file) {
		if (file == null || file.isEmpty()) {
			return false;
		}

		String contentType = file.getContentType();

		return file.getSize() <= MAX_IMAGE_SIZE_BYTES && contentType != null
				&& (contentType.equalsIgnoreCase("image/jpeg") || contentType.equalsIgnoreCase("image/png")
						|| contentType.equalsIgnoreCase("image/webp"));
	}

	private String extractPublicIdFromUrl(String imageUrl) {
		if (imageUrl == null || imageUrl.isBlank()) {
			return null;
		}

		int uploadIndex = imageUrl.indexOf("/upload/");
		if (uploadIndex == -1) {
			return null;
		}

		String pathAfterUpload = imageUrl.substring(uploadIndex + "/upload/".length());

		if (pathAfterUpload.matches("^v\\d+/.*")) {
			pathAfterUpload = pathAfterUpload.substring(pathAfterUpload.indexOf("/") + 1);
		}

		int extensionIndex = pathAfterUpload.lastIndexOf(".");
		if (extensionIndex > 0) {
			pathAfterUpload = pathAfterUpload.substring(0, extensionIndex);
		}

		return pathAfterUpload;
	}
}