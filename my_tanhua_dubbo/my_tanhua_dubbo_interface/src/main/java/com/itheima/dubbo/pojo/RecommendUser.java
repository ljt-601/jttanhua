package com.itheima.dubbo.pojo;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

//这个类用于dubbo和其他服务之间的传输
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "recommend_user") //指定mongodb中的表名，完成映射
public class RecommendUser implements Serializable {
    private static final long serialVersionUID = -4296017160071130962L;

    @Id
    private ObjectId id; //主键id

    @Indexed
    private Long userId; //推荐的用户id
    private Long toUserId; //用户id

    @Indexed
    private Double score; //推荐得分
    private String date; //日期

}
