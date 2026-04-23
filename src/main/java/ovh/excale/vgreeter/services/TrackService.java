package ovh.excale.vgreeter.services;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ovh.excale.vgreeter.entity.TrackEntity;
import ovh.excale.vgreeter.repository.TrackRepository;

import java.util.Random;

@RequiredArgsConstructor
@Service
public class TrackService {

	public static final int DEFAULT_PAGE_SIZE = 15;

	private final TrackRepository trackRepo;

	private final Random random = new Random();

	/**
	 * Get a random track from the database.
	 *
	 * @return a random track, or null if no tracks are available
	 */
	public @Nullable TrackEntity randomTrack() {

		// Get random track index (not id)
		int qty = (int) trackRepo.count();
		int idx = random.nextInt(qty);

		// Retrieve a single track using pagination
		Page<TrackEntity> trackPage = trackRepo.findAll(PageRequest.of(idx, 1));
		TrackEntity track = null;

		// Return the track if found
		if(trackPage.hasContent())
			track = trackPage.getContent()
					.getFirst();

		return track;
	}

	/**
	 * Calculate the total number of pages for a given page size.
	 *
	 * @param pageSize the number of tracks per page
	 * @return the total number of pages
	 */
	public int totalPages(int pageSize) {

		// Get total number of track
		long totalTracks = trackRepo.count();

		// Calculate the total number of pages
		return (int) Math.ceil((double) totalTracks / pageSize);
	}
}
