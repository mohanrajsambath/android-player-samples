package com.brightcove.player.samples.offlineplayback;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.brightcove.player.edge.OfflineCatalog;
import com.brightcove.player.model.Video;
import com.brightcove.player.network.DownloadStatus;
import com.brightcove.player.samples.offlineplayback.utils.ViewUtil;
import com.squareup.picasso.Picasso;

import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Video list adapter can be used to show a list of videos on a {@link RecyclerView}.
 */
public class VideoListAdapter extends RecyclerView.Adapter<VideoListAdapter.ViewHolder> {

    /**
     * The current list of videos.
     */
    private List<Video> videoList;
    /**
     * A map of video identifiers and position of video in the list.
     */
    private Map<String, Integer> indexMap = new HashMap<>();

    /**
     * A view holder that hold references to the UI components in a list item.
     */
    public class ViewHolder extends RecyclerView.ViewHolder {
        /**
         * Reference the item view context
         */
        public final Context context;
        /**
         * Reference to the thumbnail image view.
         */
        public final ImageView videoThumbnailImage;
        /**
         * Reference to the video title view.
         */
        public final TextView videoTitleText;
        /**
         * Reference to the video status information text view.
         */
        public final TextView videoStatusText;
        /**
         * Reference to the video license information text view.
         */
        public final TextView videoLicenseText;
        /**
         * Reference to the video duration view.
         */
        public final TextView videoDurationText;
        /**
         * Reference to the rent video button.
         */
        public final Button rentButton;
        /**
         * Reference to the buy video button.
         */
        public final Button buyButton;
        /**
         * Reference to the download video button.
         */
        public final ImageButton downloadButton;
        /**
         * Reference to the delete video button.
         */
        public final ImageButton deleteButton;
        /**
         * Reference to the download progress bar.
         */
        public final ContentLoadingProgressBar downloadProgressBar;

        /**
         * The currently linked video.
         */
        public Video video;

        /**
         * Constructs a new view holder for the given item view.
         *
         * @param itemView reference to the item.
         */
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            context = itemView.getContext();
            videoThumbnailImage = ViewUtil.findView(itemView, R.id.video_thumbnail_image);
            videoTitleText = ViewUtil.findView(itemView, R.id.video_title_text);
            videoStatusText = ViewUtil.findView(itemView, R.id.video_status_text);
            videoLicenseText = ViewUtil.findView(itemView, R.id.video_license_text);
            videoDurationText = ViewUtil.findView(itemView, R.id.video_duration_text);
            rentButton = ViewUtil.findView(itemView, R.id.rent_button);
            buyButton = ViewUtil.findView(itemView, R.id.buy_button);
            downloadButton = ViewUtil.findView(itemView, R.id.download_button);
            deleteButton = ViewUtil.findView(itemView, R.id.delete_button);
            downloadProgressBar = ViewUtil.findView(itemView, R.id.download_progress_bar);
            downloadProgressBar.getProgressDrawable().setColorFilter(Color.DKGRAY, PorterDuff.Mode.SRC_IN);
        }

        @NonNull
        public Video getVideo() {
            Video result = video;

            if (video.isOfflinePlaybackAllowed()) {
                Video offlineVideo = catalog.findOfflineVideoById(video.getId());
                if (offlineVideo != null) {
                    result = offlineVideo;
                }
            }

            return result;
        }
    }

    /**
     * The catalog where the video in this list can be found.
     */
    private final OfflineCatalog catalog;

    /**
     * The listener that can handle user interactions on a video item display.
     */
    private final VideoListListener listener;

    /**
     * Constructors a new video list adapter.
     *
     * @param catalog  reference to the offline catalog.
     * @param listener reference to a listener
     * @throws IllegalArgumentException if the catalog or listener is null.
     */
    public VideoListAdapter(@NonNull OfflineCatalog catalog, @NonNull VideoListListener listener) {
        if (listener == catalog) {
            throw new IllegalArgumentException("Catalog is null!");
        }
        if (listener == null) {
            throw new IllegalArgumentException("Video list listener is null!");
        }
        this.catalog = catalog;
        this.listener = listener;
    }

    /**
     * Sets the list of videos to be shown. If this value is null, then the list will be empty.
     *
     * @param videoList list of {@link Video} objects.
     */
    public void setVideoList(@Nullable List<Video> videoList) {
        this.videoList = videoList;
        buildIndexMap();
    }

    /**
     * Build the index map.
     */
    private void buildIndexMap() {
        indexMap.clear();
        if (videoList != null) {
            int index = 0;
            for (Video video : videoList) {
                indexMap.put(video.getId(), index++);
            }
        }
        notifyDataSetChanged();
    }

    /**
     * Causes item display for the specified video to be updated.
     *
     * @param video the video that changed
     */
    public void notifyVideoChanged(@NonNull Video video) {
        notifyVideoChanged(video, null);
    }

    /**
     * Causes item display for the specified video to be updated.
     *
     * @param video  the video that changed
     * @param status optional current download status.
     */
    public void notifyVideoChanged(@NonNull Video video, @Nullable DownloadStatus status) {
        String videoId = video.getId();
        if (indexMap.containsKey(videoId)) {
            int index = indexMap.get(videoId);
            videoList.set(index, video);
            notifyItemChanged(index, status);
        }
    }

    /**
     * Removes a video from the list.
     *
     * @param video the video to be removed.
     */
    public void removeVideo(Video video) {
        String videoId = video.getId();
        if (indexMap.containsKey(videoId)) {
            int index = indexMap.remove(videoId);
            videoList.remove(index);
            buildIndexMap();
        }
    }

    /**
     * The current list of {@link Video} objects.
     *
     * @return null or a list of list of {@link Video} objects.
     */
    @Nullable
    public List<Video> getVideoList() {
        return videoList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.video_item_view, parent, false);

        return new ViewHolder(view);
    }

    /**
     * Updates the row at the given position to reflect the changes in the underlying data.
     *
     * @param holder   reference to the view holder.
     * @param position the position of the row that should be updated.
     * @param status   optional current download status.
     */
    public void updateView(@NonNull final ViewHolder holder, int position, @Nullable DownloadStatus status) {
        holder.video = videoList.get(position);
        final Video video = holder.getVideo();

        if (video.isOfflinePlaybackAllowed()) {
            final Date expiryDate = video.getLicenseExpiryDate();
            if (expiryDate == null) {
                holder.videoLicenseText.setText("Buy or rent for offline playback");
                holder.videoStatusText.setVisibility(View.GONE);
                holder.rentButton.setVisibility(View.VISIBLE);
                holder.buyButton.setVisibility(View.VISIBLE);
                holder.deleteButton.setVisibility(View.GONE);
                holder.downloadButton.setVisibility(View.GONE);
                holder.downloadProgressBar.setVisibility(View.GONE);
            } else {
                holder.rentButton.setVisibility(View.GONE);
                holder.buyButton.setVisibility(View.GONE);

                if (video.isOwned()) {
                    holder.videoLicenseText.setText("Owned");
                } else {
                    holder.videoLicenseText.setText(String.format("Rental Expires: %s %s",
                            DateFormat.getMediumDateFormat(holder.context).format(expiryDate),
                            DateFormat.getTimeFormat(holder.context).format(expiryDate)));
                }

                if (status == null) {
                    holder.videoStatusText.setText("Checking download status...");
                    holder.videoStatusText.setVisibility(View.VISIBLE);
                    holder.deleteButton.setVisibility(View.GONE);
                    holder.downloadButton.setVisibility(View.GONE);
                    holder.downloadProgressBar.setVisibility(View.GONE);

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            final DownloadStatus status = catalog.getVideoDownloadStatus(holder.video);
                            holder.itemView.post(new Runnable() {
                                @Override
                                public void run() {
                                    updateDownloadStatus(holder, status);
                                }
                            });
                        }
                    }).start();

                } else {
                    updateDownloadStatus(holder, status);
                }
            }
        } else {
            holder.videoLicenseText.setVisibility(View.GONE);
            holder.videoStatusText.setText("Online Only");
            holder.videoStatusText.setVisibility(View.VISIBLE);
            holder.rentButton.setVisibility(View.GONE);
            holder.buyButton.setVisibility(View.GONE);
            holder.downloadButton.setVisibility(View.GONE);
            holder.deleteButton.setVisibility(View.GONE);
            holder.downloadProgressBar.setVisibility(View.GONE);
        }

        holder.videoTitleText.setText(video.getName());

        URI imageUri = video.getStillImageUri();
        if (imageUri == null) {
            holder.videoThumbnailImage.setImageResource(R.drawable.movie);
        } else {
            Picasso.with(holder.context).load(imageUri.toASCIIString()).into(holder.videoThumbnailImage);
        }

        int duration = video.getDuration();
        if (duration > 0) {
            holder.videoDurationText.setText(millisecondsToString(duration));
            holder.videoDurationText.setVisibility(View.VISIBLE);
        } else {
            holder.videoDurationText.setText(null);
            holder.videoDurationText.setVisibility(View.GONE);
        }

    }

    /**
     * Updates the view held by the specified holder with the current status of the download.
     *
     * @param holder reference to the view holder.
     * @param status optional current download status.
     */
    private void updateDownloadStatus(@NonNull ViewHolder holder, DownloadStatus status) {
        int statusCode = status.getCode();
        if (statusCode == DownloadStatus.STATUS_NOT_QUEUED) {
            holder.videoStatusText.setText("Press download to save video");
            holder.downloadButton.setVisibility(View.VISIBLE);
            holder.downloadProgressBar.setVisibility(View.GONE);
        } else {
            holder.videoStatusText.setText(status.getStatusMessage());
            holder.downloadButton.setVisibility(View.GONE);

            if (statusCode == DownloadStatus.STATUS_DOWNLOADING) {
                holder.downloadProgressBar.setVisibility(View.VISIBLE);
                holder.downloadProgressBar.setProgress((int) status.getProgress());
            } else {
                holder.downloadProgressBar.setVisibility(View.GONE);
                if (statusCode == DownloadStatus.STATUS_DELETING) {
                    holder.videoLicenseText.setVisibility(View.GONE);
                    holder.rentButton.setVisibility(View.GONE);
                    holder.buyButton.setVisibility(View.GONE);
                    holder.downloadButton.setVisibility(View.GONE);
                    holder.deleteButton.setVisibility(View.GONE);
                }
            }
        }

        holder.videoStatusText.setVisibility(View.VISIBLE);
        holder.deleteButton.setVisibility(View.VISIBLE);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position, List<Object> payloads) {
        if (payloads.size() == 0) {
            super.onBindViewHolder(holder, position, payloads);
        } else {
            updateView(holder, position, (DownloadStatus) payloads.get(0));
        }
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        updateView(holder, position, null);

        // Setup listeners to handle user interactions
        holder.videoThumbnailImage.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                boolean consumed = false;
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    consumed = true;
                }
                if(event.getAction() == MotionEvent.ACTION_UP) {
                    listener.playVideo(holder.getVideo());
                    consumed = true;
                }
                return consumed;
            }
        });
        holder.rentButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    listener.rentVideo(holder.video);
                }
                return false;
            }
        });
        holder.buyButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    listener.buyVideo(holder.video);
                }
                return false;
            }
        });
        holder.downloadButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    listener.downloadVideo(holder.video);
                }
                return false;
            }
        });
        holder.deleteButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    holder.videoStatusText.setText("Deleting video...");
                    listener.deleteVideo(holder.video);
                }
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return videoList == null ? 0 : videoList.size();
    }

    /**
     * Converts the given duration into a time span string.
     *
     * @param duration elapsed time as number of milliseconds.
     * @return the formatted time span string.
     */
    @NonNull
    public static String millisecondsToString(long duration) {
        final TimeUnit scale = TimeUnit.MILLISECONDS;

        StringBuilder builder = new StringBuilder();
        long days = scale.toDays(duration);
        duration -= TimeUnit.DAYS.toMillis(days);
        if (days > 0) {
            builder.append(days);
            builder.append(days > 1 ? " days " : " day ");
        }

        long hours = scale.toHours(duration);
        duration -= TimeUnit.HOURS.toMillis(hours);
        if (hours > 0) {
            builder.append(String.format("%02d:", hours));
        }

        long minutes = scale.toMinutes(duration);
        duration -= TimeUnit.MINUTES.toMillis(minutes);

        long seconds = scale.toSeconds(duration);
        builder.append(String.format("%02d:%02d", minutes, seconds));

        return builder.toString();
    }
}