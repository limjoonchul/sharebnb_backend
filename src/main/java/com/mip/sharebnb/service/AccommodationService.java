package com.mip.sharebnb.service;

import com.mip.sharebnb.dto.AccommodationDto;
import com.mip.sharebnb.dto.SearchAccommodationDto;
import com.mip.sharebnb.exception.DataNotFoundException;
import com.mip.sharebnb.exception.InvalidInputException;
import com.mip.sharebnb.model.Accommodation;
import com.mip.sharebnb.repository.AccommodationPictureRepository;
import com.mip.sharebnb.repository.AccommodationRepository;
import com.mip.sharebnb.repository.BookedDateRepository;
import com.mip.sharebnb.repository.ReviewRepository;
import com.mip.sharebnb.repository.dynamic.DynamicAccommodationRepository;
import com.mip.sharebnb.security.jwt.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;

@Transactional
@Service
@RequiredArgsConstructor
public class AccommodationService {

    private final AccommodationPictureRepository accommodationPictureRepository;
    private final DynamicAccommodationRepository dynamicAccRepository;
    private final BookedDateRepository bookedDateRepository;
    private final AccommodationRepository accRepository;
    private final ReviewRepository reviewRepository;
    private final TokenProvider tokenProvider;

    public AccommodationDto findById(HttpServletRequest request, Long id) {
        AccommodationDto accommodationDto = dynamicAccRepository.findById(parseRequestToMemberId(request), id);

        if (accommodationDto == null) {
            throw new DataNotFoundException("Accommodation Not Found");
        }

        return setListObjects(accommodationDto);
    }

    public Page<Accommodation> findAccommodations(Pageable pageable) {

        return accRepository.findAccommodationsBy(pageable);
    }

    public Page<SearchAccommodationDto> findByCity(HttpServletRequest request, String city, Pageable page) {

        return dynamicAccRepository.findByCity(city, parseRequestToMemberId(request), page);
    }

    public Page<SearchAccommodationDto> findByBuildingType(HttpServletRequest request, String buildingType, Pageable page) {

        return dynamicAccRepository.findByBuildingType(buildingType, parseRequestToMemberId(request), page);
    }

    public Page<SearchAccommodationDto> findAccommodationsBySearch(HttpServletRequest request, String searchKeyword,
                                                                   LocalDate checkIn, LocalDate checkout,
                                                                   int guestNum, Pageable page) {

        if (checkIn != null && checkout != null && checkout.isBefore(checkIn)) {
            throw new InvalidInputException("Checkout time is before check-in time");
        }

        if (checkout != null && checkout.isBefore(LocalDate.now())) {
            throw new InvalidInputException("Checkout time has passed");
        }

        if (checkIn != null && checkIn.isBefore(LocalDate.now())) {
            throw new InvalidInputException("Check-in time has passed");
        }

        if (checkIn == null) {
            checkIn = LocalDate.now();
        }

        return dynamicAccRepository.findAccommodationsBySearch(searchKeyword, checkIn, checkout, guestNum, parseRequestToMemberId(request), page);
    }

    public Page<SearchAccommodationDto> findAccommodationsByMapSearch(HttpServletRequest request, float minLatitude, float maxLatitude,
                                                                      float minLongitude, float maxLongitude, Pageable page) {

        return dynamicAccRepository.findAccommodationsByMapSearch(minLatitude, maxLatitude, minLongitude, maxLongitude, parseRequestToMemberId(request), page);
    }

    private AccommodationDto setListObjects(AccommodationDto acc) {

        acc.setAccommodationPictures(accommodationPictureRepository.findByAccommodationId(acc.getId()));
        acc.setBookedDates(bookedDateRepository.findBookedDatesByAccommodationId(acc.getId()));
        acc.setReviews(reviewRepository.findReviewsByAccommodationId(acc.getId()));

        return acc;
    }

    private Long parseRequestToMemberId(HttpServletRequest request) {
        Long memberId = null;

        if (request != null) {
            String token = request.getHeader("Authorization");

            if (token != null) {
                memberId = tokenProvider.parseTokenToGetUserId(token);
            }
        }

        return memberId;
    }
}