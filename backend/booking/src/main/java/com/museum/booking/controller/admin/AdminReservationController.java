package com.museum.booking.controller.admin;

import com.museum.booking.common.PageResult;
import com.museum.booking.common.Result;
import com.museum.booking.dto.ReservationDtos;
import com.museum.booking.security.RequireRole;
import com.museum.booking.security.UserContext;
import com.museum.booking.service.ReservationService;
import com.museum.booking.vo.ReservationVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

/**
 * 后台预约管理接口。
 */
@RestController
@RequestMapping("/api/admin/reservations")
@RequiredArgsConstructor
public class AdminReservationController {

    private final ReservationService reservationService;

    @GetMapping
    @RequireRole({"ADMIN", "STAFF"})
    public Result<PageResult<ReservationVO>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate visitDate,
            @RequestParam(required = false) Long exhibitionId) {
        return Result.ok(reservationService.adminPage(page, size, keyword, status, visitDate, exhibitionId));
    }

    @GetMapping("/export")
    @RequireRole("ADMIN")
    public ResponseEntity<byte[]> export(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate visitDate,
            @RequestParam(required = false) Long exhibitionId) {
        String csv = reservationService.exportCsv(keyword, status, visitDate, exhibitionId);
        byte[] bytes = ("\uFEFF" + csv).getBytes(StandardCharsets.UTF_8);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv;charset=UTF-8"));
        headers.setContentDisposition(ContentDisposition.attachment().filename("reservations.csv").build());
        return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @RequireRole({"ADMIN", "STAFF"})
    public Result<ReservationVO> detail(@PathVariable Long id) {
        return Result.ok(reservationService.detail(id, UserContext.require()));
    }

    @PostMapping("/{id}/checkin")
    @RequireRole({"ADMIN", "STAFF"})
    public Result<ReservationVO> checkin(@PathVariable Long id) {
        ReservationVO detail = reservationService.detail(id, UserContext.require());
        return Result.ok(reservationService.checkin(detail.getOrderNo(), UserContext.require()));
    }

    @PutMapping("/{id}/cancel")
    @RequireRole("ADMIN")
    public Result<Void> cancel(@PathVariable Long id,
                               @Valid @RequestBody(required = false) ReservationDtos.CancelRequest request) {
        reservationService.cancel(UserContext.require().id(), id,
                request == null ? "管理员取消" : request.reason(), true);
        return Result.ok();
    }
}