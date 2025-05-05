package foodies.service;

import foodies.io.FoodRequest;
import foodies.io.FoodResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FoodService
{
   /**
    *
    * @param file
    * @return
    */
   String uploadFile(MultipartFile file);

   /**
    *
    * @param request
    * @param file
    * @return
    */
   FoodResponse addFood(FoodRequest request, MultipartFile file);

   /**
    *
    * @return
    */
   List<FoodResponse> readFoods();

   /**
    *
    * @param id
    * @return
    */
   FoodResponse readFood(String id);

   /**
    *
    * @param filename
    * @return
    */
   boolean deleteFile(String filename);

   /**
    *
    * @param id
    */
   void deleteFood(String id);
}
