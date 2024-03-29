package ovh.excale.vgreeter.services;

import lombok.Getter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ovh.excale.vgreeter.models.TrackModel;
import ovh.excale.vgreeter.repositories.TrackRepository;

@Service
public class TrackService {

	public static final int DEFAULT_MAX_TRACK_SIZE = 1024 * 64;

	@Getter
	private final TrackRepository trackRepo;

	public TrackService(TrackRepository trackRepo) {
		this.trackRepo = trackRepo;
	}

	// TODO: nullsafe
	public TrackModel randomTrack() {

		long qty = trackRepo.count();
		int idx = (int) (Math.random() * qty);

		Page<TrackModel> trackPage = trackRepo.findAll(PageRequest.of(idx, 1));
		TrackModel track = null;
		if(trackPage.hasContent())
			track = trackPage.getContent()
					.get(0);

		return track;
	}

}
