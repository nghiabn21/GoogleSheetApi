package com.GoogleSheetAPI.entity;

import com.GoogleSheetAPI.dto.TitleInfo;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PersonInfo {
    private String name;
    private String phone;
    private String email;
    private String address;
}
