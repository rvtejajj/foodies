package foodies.service;

import foodies.entity.FoodEntity;
import foodies.io.FoodRequest;
import foodies.io.FoodResponse;
import foodies.repository.FoodRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
public class FoodServiceImpl implements FoodService
{
   @Autowired
   private  S3Client s3Client;

   @Autowired
   private  FoodRepository foodRepository;

   @Value("${aws.s3.bucketName}")
   private String bucketName;



   @Override
   public FoodResponse addFood(FoodRequest request, MultipartFile file)
   {
      FoodEntity newFoodEntity = convertToEntity(request);
      String imageUrl = uploadFile(file);
      newFoodEntity.setImageUrl(imageUrl);
      newFoodEntity = foodRepository.save(newFoodEntity);
      return convertToResponse(newFoodEntity);
   }

   @Override
   public String uploadFile(MultipartFile file)
   {
      String filenameExtension = file.getOriginalFilename()
               .substring(file.getOriginalFilename().lastIndexOf(".") + 1);
      String key = String.join(UUID.randomUUID().toString(), ".", filenameExtension);

      try
      {
         PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                  .bucket(bucketName)
                  .key(key)
                  .acl("public-read")
                  .contentType(file.getContentType())
                  .build();
         PutObjectResponse response =
                  s3Client.putObject(putObjectRequest, RequestBody.fromBytes(file.getBytes()));

         if (response.sdkHttpResponse().isSuccessful())
         {
            return "https://" + bucketName + ".s3.amazonaws.com/" + key;
         }
         else
         {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                     "File upload failed");
         }
      }
      catch (IOException ex)
      {
         throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                  "An error occured while uploading the file");
      }
   }



   private FoodEntity convertToEntity(FoodRequest request)
   {
      return FoodEntity.builder()
               .name(request.getName())
               .description(request.getDescription())
               .category(request.getCategory())
               .price(request.getPrice())
               .build();

   }

   private FoodResponse convertToResponse(FoodEntity entity)
   {
      return FoodResponse.builder()
               .id(entity.getId())
               .name(entity.getName())
               .description(entity.getDescription())
               .category(entity.getCategory())
               .price(entity.getPrice())
               .imageUrl(entity.getImageUrl())
               .build();
   }

   @Override
   public List<FoodResponse> readFoods()
   {
      List<FoodEntity> entries = foodRepository.findAll();
      return entries.stream().map(this::convertToResponse).toList();
   }

   @Override
   public FoodResponse readFood(String id)
   {
      FoodEntity entry = foodRepository.findById(id)
               .orElseThrow(() -> new RuntimeException("no recored present" + id));
      return convertToResponse(entry);
   }

   @Override
   public boolean deleteFile(String filename)
   {
      DeleteObjectRequest fileName =
               DeleteObjectRequest.builder().bucket(bucketName).key(filename).build();
      DeleteObjectResponse deleteObjectResponse =
               s3Client.deleteObject(fileName);
      return deleteObjectResponse != null;
   }

   @Override
   public void deleteFood(String id)
   {
      FoodResponse foodResponse = readFood(id);
      String imageUrl = foodResponse.getImageUrl();
      String fileName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
      if (deleteFile(fileName))
      {
         foodRepository.deleteById(id);
      }
   }
}
