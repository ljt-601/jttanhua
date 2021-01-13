package com.tanhua.server.controller;

import com.tanhua.server.service.CommentsService;
import com.tanhua.server.vo.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("comments")
public class CommentsController {
    @Autowired
    private CommentsService commentsService;

    @GetMapping
    //查询评论列表
    public ResponseEntity<PageResult> queryCommentsList(@RequestParam("movementId")String publishId,
                                                        @RequestParam(value = "page",defaultValue = "1")Integer page,
                                                        @RequestParam(value = "pagesize",defaultValue ="10")Integer pageSize){
        try {
            PageResult pageResult=this.commentsService.queryCommentsList(publishId,page,pageSize);
            if (null!=pageResult){
                return ResponseEntity.ok(pageResult);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();

    }
    @PostMapping
    //发表评论
    public ResponseEntity<Void> saveComments(@RequestBody Map<String,String> param){

         try {
             String movementId = param.get("movementId");
             String content = param.get("comment");
             boolean bool= this.commentsService.saveComments(movementId,content);
             if (bool){
                 return ResponseEntity.ok(null);
             }
         }catch (Exception e){
             e.printStackTrace();
         }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();

    }

}
