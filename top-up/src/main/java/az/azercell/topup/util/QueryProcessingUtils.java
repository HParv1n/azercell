package az.azercell.topup.util;

import az.azercell.topup.dto.PageableDataDTO;
import az.azercell.topup.model.base.BaseDTO;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.function.Function;

@Slf4j
public final class QueryProcessingUtils
{
    private static final int MIN_SORT_LENGTH = 2;
    private static final String SORT_SELECTOR_FIELD = "selector";
    private static final String SORT_DESC_FIELD = "desc";
    private static final String DEFAULT_SORT_COLUMN = "id";


    private QueryProcessingUtils()
    {
        // Prevent instantiation
    }

    private static JsonNode parseParameter(String param, int minLength)
    {
        String decodedParam = UriUtils.decode(param, StandardCharsets.UTF_8);

        if (StringUtil.isEmptyOrNull(decodedParam) || decodedParam.trim().length() <= minLength)
        {
            return null;
        }

        try
        {
            return JsonUtil.parseJsonArray(decodedParam);
        }
        catch (IOException e)
        {
            log.error("Error parsing parameter: {}", e.getMessage());
            return null;
        }
    }

    public static JsonNode parseSortParameter(String sort)
    {
        JsonNode sortNode = parseParameter(sort, MIN_SORT_LENGTH);
        return sortNode != null && sortNode.isArray() && sortNode.size() > 0 ? sortNode.get(0) : null;
    }

    public static Sort resolveSortCriteria(JsonNode sortNode)
    {
        Sort.Direction direction = Sort.Direction.ASC;
        String         column    = DEFAULT_SORT_COLUMN;

        if (sortNode != null)
        {
            String  selector = sortNode.get(SORT_SELECTOR_FIELD).asText();
            boolean desc     = sortNode.get(SORT_DESC_FIELD).asBoolean();
            direction = desc ? Sort.Direction.DESC : Sort.Direction.ASC;
            column = selector;
        }

        return Sort.by(direction, column);
    }

    public static <T extends BaseDTO> Page<T> loadPageResults(Pageable pageable,
                                                              Function<Pageable, Page<T>> findAllFunction)
    {
        return findAllFunction.apply(pageable);
    }

    public static Pageable createPageRequest(int pageSize, int offset, Sort sort)
    {
        int pageNumber = offset / pageSize;
        return PageRequest.of(pageNumber, pageSize, sort);
    }

    public static <T extends BaseDTO> ResponseEntity<Object> buildPageResponseEntity(Page<T> page)
    {
        PageableDataDTO pageableDataDTO = new PageableDataDTO(page.getContent(), page.getTotalElements());
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
            ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return new ResponseEntity<>(pageableDataDTO, headers, HttpStatus.OK);
    }
}
