package com.tanhua.server.vo;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovementsParam {

    private String textContent;
    private String location;
    private String longitude;
    private String latitude;
}
