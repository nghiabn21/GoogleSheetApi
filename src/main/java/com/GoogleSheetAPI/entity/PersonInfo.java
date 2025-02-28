package com.GoogleSheetAPI.entity;

import lombok.*;

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
