package com.tanhua.server.controller;


import com.tanhua.server.service.MovementsService;
import com.tanhua.server.vo.Movements;
import com.tanhua.server.vo.MovementsParam;
import com.tanhua.server.vo.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("movements")
public class MovementsController {

    @Autowired
    private MovementsService movementsService;

    @PostMapping
    public ResponseEntity<Void> saveMovements(MovementsParam movementsParam, @RequestParam(value = "imageContent", required = false) MultipartFile[] multipartFile){
        try {
            Boolean bool= this.movementsService.saveMovements(movementsParam,multipartFile);
            if (bool){
                return ResponseEntity.ok(null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
    @GetMapping
    //查询好友的动态信息
    public ResponseEntity<PageResult> queryPublishList(@RequestParam(value = "page", defaultValue = "1") Integer page,@RequestParam(value = "pagesize", defaultValue = "10") Integer pageSize){
        try {
            PageResult pageResult= this.movementsService.queryUserPublishList(page,pageSize);
            return ResponseEntity.ok(pageResult);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();

    }

    @GetMapping("recommend")
    //查询推荐的动态信息
    public ResponseEntity<PageResult> queryRecommendPublishList(@RequestParam(value = "page", defaultValue = "1") Integer page,@RequestParam(value = "pagesize", defaultValue = "10") Integer pageSize){
        try {
            PageResult pageResult= this.movementsService.queryRecommendPublishList(page,pageSize);
            return ResponseEntity.ok(pageResult);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
    //根据路径中拼接的动态Id查询并进行点赞操作，并返回点赞数
    @GetMapping("/{id}/like")
    //@Cacheable(value = "likeComment")
    public ResponseEntity<Long> likeComment(@PathVariable("id") String publishId){

        try {
            Long count=this.movementsService.likeComment(publishId);
            if (null!=count){
                return ResponseEntity.ok(count);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    //根据路径中拼接的动态Id查询并进行取消点赞的操作，并返回点赞数
    @GetMapping("/{id}/dislike")
    //@Cacheable(value = "likeComment")
    public ResponseEntity<Long> disLikeComment(@PathVariable("id") String publishId){

        try {
            Long count=this.movementsService.disLikeComment(publishId);
            if (null!=count){
                return ResponseEntity.ok(count);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    } //根据路径中拼接的动态Id查询并进行喜欢操作，并返回点赞数
    @GetMapping("/{id}/love")
    //@Cacheable(value = "likeComment")
    public ResponseEntity<Long> loveComment(@PathVariable("id") String publishId){

        try {
            Long count=this.movementsService.loveComment(publishId);
            if (null!=count){
                return ResponseEntity.ok(count);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    //根据路径中拼接的动态Id查询并进行取消喜欢的操作，并返回点赞数
    @GetMapping("/{id}/unlove")
    //@Cacheable(value = "likeComment")
    public ResponseEntity<Long> unLoveComment(@PathVariable("id") String publishId){

        try {
            Long count=this.movementsService.unLoveComment(publishId);
            if (null!=count){
                return ResponseEntity.ok(count);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    @GetMapping("{id}")
    //获取单条动态展示
    public ResponseEntity<Movements> queryMovementsById(@PathVariable("id") String publishId){

        try {
            Movements movements= this.movementsService.queryMovementsById(publishId);
            if (null!=movements){
                return ResponseEntity.ok(movements);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

}
