package com.su.model.common.wemedia.dtos;

import com.su.model.common.dtos.PageRequestDto;
import lombok.Data;

@Data
public class WmMaterialDto extends PageRequestDto {
    private Short isCollection;
}
