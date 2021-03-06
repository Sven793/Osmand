package net.osmand.plus.views;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorCompat;
import android.support.v4.view.ViewPropertyAnimatorListener;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.PopupMenu;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import net.osmand.AndroidUtils;
import net.osmand.core.android.MapRendererContext;
import net.osmand.data.LatLon;
import net.osmand.data.PointDescription;
import net.osmand.data.RotatedTileBox;
import net.osmand.plus.IconsCache;
import net.osmand.plus.OsmAndLocationProvider;
import net.osmand.plus.OsmandApplication;
import net.osmand.plus.OsmandPlugin;
import net.osmand.plus.OsmandSettings;
import net.osmand.plus.OsmandSettings.CommonPreference;
import net.osmand.plus.OsmandSettings.LayerTransparencySeekbarMode;
import sd.smartroute.R;
import net.osmand.plus.TargetPointsHelper;
import net.osmand.plus.TargetPointsHelper.TargetPoint;
import net.osmand.plus.Version;
import net.osmand.plus.activities.HelpActivity;
import net.osmand.plus.activities.MapActivity;
import net.osmand.plus.activities.MapActivity.ShowQuickSearchMode;
import net.osmand.plus.activities.MapBottomButtonsDialogFragment;
import net.osmand.plus.activities.search.SearchActivity;
import net.osmand.plus.dashboard.DashboardOnMap;
import net.osmand.plus.dashboard.DashboardOnMap.DashboardType;
import net.osmand.plus.dialogs.DirectionsDialogs;
import net.osmand.plus.download.IndexItem;
import net.osmand.plus.liveupdates.OsmLiveActivity;
import net.osmand.plus.mapcontextmenu.MapContextMenu;
import net.osmand.plus.mapcontextmenu.other.MapRouteInfoMenu;
import net.osmand.plus.mapcontextmenu.other.TrackDetailsMenu;
import net.osmand.plus.routing.RoutingHelper;
import net.osmand.plus.views.corenative.NativeCoreContext;

import java.util.ArrayList;
import java.util.List;

import gnu.trove.list.array.TIntArrayList;
import sd.smartroute.Activity.SetupActivity;
import sd.smartroute.Config.Singleton;

public class MapControlsLayer extends OsmandMapLayer {

	private static final int TIMEOUT_TO_SHOW_BUTTONS = 7000;
	private static final int REQUEST_LOCATION_FOR_NAVIGATION_PERMISSION = 200;
	private static final int REQUEST_LOCATION_FOR_NAVIGATION_FAB_PERMISSION = 201;
	private static final int REQUEST_LOCATION_FOR_ADD_DESTINATION_PERMISSION = 202;

	public MapHudButton createHudButton(View iv, int resId) {
		MapHudButton mc = new MapHudButton();
		mc.iv = iv;
		mc.resId = resId;
		return mc;
	}

	private List<MapHudButton> controls = new ArrayList<>();
	private final MapActivity mapActivity;
	private int shadowColor = -1;
	// private RulerControl rulerControl;
	// private List<MapControls> allControls = new ArrayList<MapControls>();

	private SeekBar transparencyBar;
	private LinearLayout transparencyBarLayout;
	private static CommonPreference<Integer> transparencySetting;
	private boolean isTransparencyBarEnabled = true;
	private OsmandSettings settings;

	private MapRouteInfoMenu mapRouteInfoMenu;
	private TrackDetailsMenu trackDetailsMenu;
	private MapHudButton backToLocationControl;
	private MapHudButton menuControl;
	private MapHudButton compassHud;
	private MapHudButton quickSearchHud;
	private float cachedRotate = 0;
	private TextView zoomText;
	private OsmandMapTileView mapView;
	private OsmandApplication app;
	private MapHudButton routePlanningBtn;
	private long touchEvent;
	private MapHudButton mapZoomOut;
	private MapHudButton mapZoomIn;
	private MapHudButton layersHud;
	private long lastZoom;
	private boolean hasTargets;
	private ContextMenuLayer contextMenuLayer;
	private MapQuickActionLayer mapQuickActionLayer;
	private boolean forceShowCompass;
	private LatLon requestedLatLon;
	private BottomSheetBehavior bottomSheetBehavior;
	private MapBottomButtonsDialogFragment mapBottomButtonsDialogFragment;

	public MapControlsLayer(MapActivity activity) {
		this.mapActivity = activity;
		app = activity.getMyApplication();
		settings = activity.getMyApplication().getSettings();
		mapView = mapActivity.getMapView();
		contextMenuLayer = mapActivity.getMapLayers().getContextMenuLayer();
		bottomSheetBehavior = mapActivity.getBottomSheetBehavior();
	}

	public MapRouteInfoMenu getMapRouteInfoMenu() {
		return mapRouteInfoMenu;
	}

	public TrackDetailsMenu getTrackDetailsMenu() {
		return trackDetailsMenu;
	}

	@Override
	public boolean drawInScreenPixels() {
		return true;
	}

	@Override
	public void initLayer(final OsmandMapTileView view) {
		initTopControls();
		initTransparencyBar();
		initZooms();
		if (settings.NEW_MAP_VIEW.get()) {
			initBottomSheetControls();
		}
		initDasboardRelatedControls();
		updateControls(view.getCurrentRotatedTileBox(), null);
	}

	public void initDasboardRelatedControls() {
		initControls();
		initRouteControls();
	}

	public View moveCompassButton(ViewGroup destLayout, ViewGroup.LayoutParams layoutParams, boolean night) {
		View compassView = compassHud.iv;
		ViewGroup parent = (ViewGroup) compassView.getParent();
		if (parent != null) {
			compassHud.cancelHideAnimation();
			compassHud.compassOutside = true;
			forceShowCompass = true;
			parent.removeView(compassView);
			compassView.setLayoutParams(layoutParams);
			destLayout.addView(compassView);
			updateCompass(night);
			return compassView;
		}
		return null;
	}

	public void restoreCompassButton(boolean night) {
		View compassView = compassHud.iv;
		ViewGroup parent = (ViewGroup) compassView.getParent();
		if (parent != null) {
			compassHud.compassOutside = false;
			forceShowCompass = false;
			parent.removeView(compassView);
			LinearLayout mapCompassContainer = (LinearLayout) mapActivity.findViewById(R.id.layers_compass_layout);
			if (mapCompassContainer != null) {
				int buttonSizePx = mapActivity.getResources().getDimensionPixelSize(R.dimen.map_small_button_size);
				int topMarginPx = mapActivity.getResources().getDimensionPixelSize(R.dimen.map_small_button_margin);
				LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(buttonSizePx, buttonSizePx);
				params.topMargin = topMarginPx;
				compassView.setLayoutParams(params);
				mapCompassContainer.addView(compassView);
				updateCompass(night);
			}
		}
	}

	private class CompassDrawable extends Drawable {

		private Drawable original;

		public CompassDrawable(Drawable original) {
			this.original = original;
		}

		@Override
		public void draw(Canvas canvas) {
			canvas.save();
			canvas.rotate(cachedRotate, getIntrinsicWidth() / 2, getIntrinsicHeight() / 2);
			original.draw(canvas);
			canvas.restore();
		}

		@Override
		public int getMinimumHeight() {
			return original.getMinimumHeight();
		}

		@Override
		public int getMinimumWidth() {
			return original.getMinimumWidth();
		}

		@Override
		public int getIntrinsicHeight() {
			return original.getIntrinsicHeight();
		}

		@Override
		public int getIntrinsicWidth() {
			return original.getIntrinsicWidth();
		}

		@Override
		public void setChangingConfigurations(int configs) {
			super.setChangingConfigurations(configs);
			original.setChangingConfigurations(configs);
		}

		@Override
		public void setBounds(int left, int top, int right, int bottom) {
			super.setBounds(left, top, right, bottom);
			original.setBounds(left, top, right, bottom);
		}

		@Override
		public void setAlpha(int alpha) {
			original.setAlpha(alpha);
		}

		@Override
		public void setColorFilter(ColorFilter cf) {
			original.setColorFilter(cf);
		}

		@Override
		public int getOpacity() {
			return original.getOpacity();
		}
	}

	private void initTopControls() {
		if (settings.NEW_MAP_VIEW.get()) {
			View compass = mapActivity.findViewById(R.id.map_compass_button);
			compassHud = createHudButton(compass, R.drawable.map_compass).setIconColorId(0).
					setBg(R.drawable.btn_inset_circle_trans, R.drawable.btn_inset_circle_night);
			compassHud.compass = true;
			controls.add(compassHud);
			compass.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					mapActivity.getMapViewTrackingUtilities().switchRotateMapMode();
				}
			});
		} else {
			View configureMap = mapActivity.findViewById(R.id.map_layers_button);
			configureMap.setVisibility(View.INVISIBLE);
			layersHud = createHudButton(configureMap, R.drawable.map_world_globe_dark)
					.setIconColorId(R.color.on_map_icon_color, 0)
					.setBg(R.drawable.btn_inset_circle_trans, R.drawable.btn_inset_circle_night);
			controls.add(layersHud);
			configureMap.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					MapActivity.clearPrevActivityIntent();
					mapActivity.getDashboard().setDashboardVisibility(true, DashboardType.CONFIGURE_MAP);
				}
			});

			View compass = mapActivity.findViewById(R.id.map_compass_button);
			compassHud = createHudButton(compass, R.drawable.map_compass).setIconColorId(0).
					setBg(R.drawable.btn_inset_circle_trans, R.drawable.btn_inset_circle_night);
			compassHud.compass = true;
			controls.add(compassHud);
			compass.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					mapActivity.getMapViewTrackingUtilities().switchRotateMapMode();
				}
			});

			View search = mapActivity.findViewById(R.id.map_search_button);
			search.setVisibility(View.INVISIBLE);
			quickSearchHud = createHudButton(search, R.drawable.map_search_dark)
					.setIconsId(R.drawable.map_search_dark, R.drawable.map_search_night)
					.setIconColorId(0)
					.setBg(R.drawable.btn_inset_circle_trans, R.drawable.btn_inset_circle_night);
			controls.add(quickSearchHud);
			search.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					mapActivity.showQuickSearch(ShowQuickSearchMode.NEW_IF_EXPIRED, false);
				}
			});
		}

	}

	private void initBottomSheetControls() {
		final DisplayMetrics metrics = new DisplayMetrics();
		mapActivity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
		bottomSheetBehavior.setPeekHeight(metrics.heightPixels / 2);
		bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
		bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
			@Override
			public void onStateChanged(@NonNull View bottomSheet, int newState) {
				switch (newState) {
					case BottomSheetBehavior.STATE_COLLAPSED:
						Log.e("Bottom Sheet Behaviour", "STATE_COLLAPSED");
						break;
					case BottomSheetBehavior.STATE_DRAGGING:
						Log.e("Bottom Sheet Behaviour", "STATE_DRAGGING");
						break;
					case BottomSheetBehavior.STATE_EXPANDED:
						Log.e("Bottom Sheet Behaviour", "STATE_EXPANDED");
						break;
					case BottomSheetBehavior.STATE_HIDDEN:
						Log.e("Bottom Sheet Behaviour", "STATE_HIDDEN");
						break;
					case BottomSheetBehavior.STATE_SETTLING:
						Log.e("Bottom Sheet Behaviour", "STATE_SETTLING");
						break;
				}
			}

			@Override
			public void onSlide(@NonNull View bottomSheet, float slideOffset) {
				Log.d(MapControlsLayer.class.getCanonicalName(), "onSlide: " + bottomSheet.getY() + "::" + bottomSheet.getMeasuredHeight() + " :: " + bottomSheet.getTop());
				Log.d(MapControlsLayer.class.getCanonicalName(), "onSlide: " + slideOffset);
//				ViewGroup.LayoutParams params = bottomSheet.getLayoutParams();
//				params.height = Math.max(0, metrics.heightPixels - (int) bottomSheet.getTop());
//				bottomSheet.setLayoutParams(params);
			}
		});

		IconsCache ic = mapActivity.getMyApplication().getIconsCache();

		View dashboardView = mapActivity.findViewById(R.id.dashboard_view);
		((ImageView) mapActivity.findViewById(R.id.dashboard_icon)).
				setImageDrawable(ic.getThemedIcon(R.drawable.map_dashboard));
		dashboardView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				MapActivity.clearPrevActivityIntent();
				mapActivity.getDashboard().setDashboardVisibility(true, DashboardOnMap.DashboardType.DASHBOARD);
				mapBottomButtonsDialogFragment.dismiss();
				bottomSheetBehavior.setHideable(true);
				bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
			}
		});

		View mapMarkersView = mapActivity.findViewById(R.id.map_markers_view);
		if (!mapActivity.getMyApplication().getSettings().USE_MAP_MARKERS.get()) {
			mapMarkersView.setVisibility(View.GONE);
		}
		((ImageView) mapActivity.findViewById(R.id.map_markers_icon)).
				setImageDrawable(ic.getThemedIcon(R.drawable.ic_action_flag_dark));
		mapMarkersView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				MapActivity.clearPrevActivityIntent();
				mapActivity.getDashboard().setDashboardVisibility(true, DashboardOnMap.DashboardType.MAP_MARKERS);
				mapBottomButtonsDialogFragment.dismiss();
				bottomSheetBehavior.setHideable(true);
				bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
			}
		});

		View waypointsView = mapActivity.findViewById(R.id.waypoints_view);
		if (mapActivity.getMyApplication().getSettings().USE_MAP_MARKERS.get()) {
			waypointsView.setVisibility(View.GONE);
		}
		((ImageView) mapActivity.findViewById(R.id.waypoints_icon)).
				setImageDrawable(ic.getThemedIcon(R.drawable.ic_action_intermediate));
		waypointsView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				MapActivity.clearPrevActivityIntent();
				mapActivity.getDashboard().setDashboardVisibility(true, DashboardOnMap.DashboardType.WAYPOINTS);
				mapBottomButtonsDialogFragment.dismiss();
				bottomSheetBehavior.setHideable(true);
				bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
			}
		});

		View myPlacesView = mapActivity.findViewById(R.id.my_places_view);
		((ImageView) mapActivity.findViewById(R.id.my_places_icon)).
				setImageDrawable(ic.getThemedIcon(R.drawable.ic_action_fav_dark));
		myPlacesView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent newIntent = new Intent(mapActivity, mapActivity.getMyApplication().getAppCustomization()
						.getFavoritesActivity());
				newIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				mapActivity.startActivity(newIntent);
				mapBottomButtonsDialogFragment.dismiss();
				bottomSheetBehavior.setHideable(true);
				bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
			}
		});

		View legacySearchView = mapActivity.findViewById(R.id.legacy_search_view);
		if (!mapActivity.getMyApplication().getSettings().SHOW_LEGACY_SEARCH.get()) {
			legacySearchView.setVisibility(View.GONE);
		}
		((ImageView) mapActivity.findViewById(R.id.legacy_search_icon)).
				setImageDrawable(ic.getThemedIcon(R.drawable.ic_action_search_dark));
		legacySearchView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent newIntent = new Intent(mapActivity, mapActivity.getMyApplication().getAppCustomization()
						.getSearchActivity());
				LatLon loc = mapActivity.getMapLocation();
				newIntent.putExtra(SearchActivity.SEARCH_LAT, loc.getLatitude());
				newIntent.putExtra(SearchActivity.SEARCH_LON, loc.getLongitude());
				if (mapActivity.getMapViewTrackingUtilities().isMapLinkedToLocation()) {
					newIntent.putExtra(SearchActivity.SEARCH_NEARBY, true);
				}
				newIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				mapActivity.startActivity(newIntent);
				mapBottomButtonsDialogFragment.dismiss();
				bottomSheetBehavior.setHideable(true);
				bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
			}
		});

		View downloadMapsView = mapActivity.findViewById(R.id.download_maps_view);
		String d = mapActivity.getMyApplication().getString(R.string.welmode_download_maps);
		if (mapActivity.getMyApplication().getDownloadThread().getIndexes().isDownloadedFromInternet) {
			List<IndexItem> updt = mapActivity.getMyApplication().getDownloadThread().getIndexes().getItemsToUpdate();
			if (updt != null && updt.size() > 0) {
				d += " (" + updt.size() + ")";
			}
		}
		((TextView) mapActivity.findViewById(R.id.download_maps_text)).
				setText(d);
		((ImageView) mapActivity.findViewById(R.id.download_maps_icon)).
				setImageDrawable(ic.getThemedIcon(R.drawable.ic_type_archive));
		downloadMapsView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent newIntent = new Intent(mapActivity, mapActivity.getMyApplication().getAppCustomization()
						.getDownloadActivity());
				newIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				mapActivity.startActivity(newIntent);
				mapBottomButtonsDialogFragment.dismiss();
				bottomSheetBehavior.setHideable(true);
				bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
			}
		});

		View osmLiveView = mapActivity.findViewById(R.id.osm_live_view);
		if (!(Version.isGooglePlayEnabled(mapActivity.getMyApplication()) || Version.isDeveloperVersion(mapActivity.getMyApplication()))) {
			osmLiveView.setVisibility(View.GONE);
		}
		((ImageView) mapActivity.findViewById(R.id.osm_live_icon)).
				setImageDrawable(ic.getThemedIcon(R.drawable.ic_action_osm_live));
		osmLiveView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(mapActivity, OsmLiveActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				mapActivity.startActivity(intent);
				mapBottomButtonsDialogFragment.dismiss();
				bottomSheetBehavior.setHideable(true);
				bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
			}
		});

		View pluginsView = mapActivity.findViewById(R.id.plugins_view);
		((ImageView) mapActivity.findViewById(R.id.plugins_icon)).
				setImageDrawable(ic.getThemedIcon(R.drawable.ic_extension_dark));
		pluginsView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent newIntent = new Intent(mapActivity, mapActivity.getMyApplication().getAppCustomization()
						.getPluginsActivity());
				newIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				mapActivity.startActivity(newIntent);
				mapBottomButtonsDialogFragment.dismiss();
				bottomSheetBehavior.setHideable(true);
				bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
			}
		});

		View configureScreenView = mapActivity.findViewById(R.id.configure_screen_view);
		((ImageView) mapActivity.findViewById(R.id.configure_screen_icon)).
				setImageDrawable(ic.getThemedIcon(R.drawable.ic_configure_screen_dark));
		configureScreenView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				MapActivity.clearPrevActivityIntent();
				mapActivity.getDashboard().setDashboardVisibility(true, DashboardOnMap.DashboardType.CONFIGURE_SCREEN);
				mapBottomButtonsDialogFragment.dismiss();
				bottomSheetBehavior.setHideable(true);
				bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
			}
		});

		View settingsView = mapActivity.findViewById(R.id.settings_view);
		((ImageView) mapActivity.findViewById(R.id.settings_icon)).
				setImageDrawable(ic.getThemedIcon(R.drawable.ic_action_settings));
		settingsView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				final Intent settings = new Intent(mapActivity, mapActivity.getMyApplication().getAppCustomization()
						.getSettingsActivity());
				settings.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				mapActivity.startActivity(settings);
				mapBottomButtonsDialogFragment.dismiss();
				bottomSheetBehavior.setHideable(true);
				bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
			}
		});

		View helpView = mapActivity.findViewById(R.id.help_view);
		((ImageView) mapActivity.findViewById(R.id.help_icon)).
				setImageDrawable(ic.getThemedIcon(R.drawable.ic_action_help));
		helpView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(mapActivity, HelpActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				mapActivity.startActivity(intent);
				mapBottomButtonsDialogFragment.dismiss();
				bottomSheetBehavior.setHideable(true);
				bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
			}
		});
	}

	private void initRouteControls() {
		mapRouteInfoMenu = new MapRouteInfoMenu(mapActivity, this);
		trackDetailsMenu = new TrackDetailsMenu(mapActivity);
	}

	public void updateRouteButtons(View main, boolean routeInfo) {
		boolean nightMode = mapActivity.getMyApplication().getDaynightHelper().isNightModeForMapControls();
		ImageView cancelRouteButton = (ImageView) main.findViewById(R.id.map_cancel_route_button);
		cancelRouteButton.setImageDrawable(app.getIconsCache().getIcon(R.drawable.map_action_cancel, !nightMode));
		AndroidUtils.setBackground(mapActivity, cancelRouteButton, nightMode, R.drawable.dashboard_button_light, R.drawable.dashboard_button_dark);
		cancelRouteButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				clickRouteCancel();
			}
		});

		ImageView waypointsButton = (ImageView) main.findViewById(R.id.map_waypoints_route_button);
		waypointsButton.setImageDrawable(app.getIconsCache().getIcon(R.drawable.map_action_waypoint, !nightMode));
		AndroidUtils.setBackground(mapActivity, waypointsButton, nightMode, R.drawable.dashboard_button_light, R.drawable.dashboard_button_dark);
		waypointsButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				clickRouteWaypoints();
			}
		});

		ImageView options = (ImageView) main.findViewById(R.id.map_options_route_button);
		options.setImageDrawable(!routeInfo ? app.getIconsCache().getIcon(R.drawable.map_action_settings,
				R.color.osmand_orange) : app.getIconsCache().getIcon(R.drawable.map_action_settings, !nightMode));
		AndroidUtils.setBackground(mapActivity, options, nightMode, R.drawable.dashboard_button_light, R.drawable.dashboard_button_dark);
		options.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				clickRouteParams();
			}
		});

		TextView routeGoButton = (TextView) main.findViewById(R.id.map_go_route_button);
		routeGoButton.setCompoundDrawablesWithIntrinsicBounds(app.getIconsCache().getIcon(R.drawable.map_start_navigation, R.color.color_myloc_distance), null, null, null);
		routeGoButton.setText(mapActivity.getString(R.string.shared_string_go));
		AndroidUtils.setTextSecondaryColor(mapActivity, routeGoButton, nightMode);
		AndroidUtils.setBackground(mapActivity, routeGoButton, nightMode, R.drawable.dashboard_button_light, R.drawable.dashboard_button_dark);
		routeGoButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				clickRouteGo();
			}
		});
	}

	public void setControlsClickable(boolean clickable) {
		for (MapHudButton mb : controls) {
			mb.iv.setClickable(clickable);
		}
	}

	private TargetPointsHelper getTargets() {
		return mapActivity.getMyApplication().getTargetPointsHelper();
	}

	protected void clickRouteParams() {
		mapActivity.getMapActions().openRoutePreferencesDialog();
	}

	protected void clickRouteWaypoints() {
		if (getTargets().checkPointToNavigateShort()) {
			mapActivity.getMapActions().openIntermediatePointsDialog();
		}
	}

	protected void clickRouteCancel() {
		mapRouteInfoMenu.hide();
		if (mapActivity.getRoutingHelper().isFollowingMode()) {
			mapActivity.getMapActions().stopNavigationActionConfirm();
		} else {
			mapActivity.getMapActions().stopNavigationWithoutConfirm();
		}
	}

	protected void clickRouteGo() {
		if (app.getTargetPointsHelper().getPointToNavigate() != null) {
			mapRouteInfoMenu.hide();
		}
		startNavigation();
	}

	public void showRouteInfoControlDialog() {
		mapRouteInfoMenu.showHideMenu();
	}

	public void showDialog() {
		mapRouteInfoMenu.setShowMenu();
	}

	private void initControls() {
		if (settings.NEW_MAP_VIEW.get()) {
			View configureMap = mapActivity.findViewById(R.id.map_layers_button);
			configureMap.setVisibility(View.INVISIBLE);
			layersHud = createHudButton(configureMap, R.drawable.map_world_globe_dark)
					.setIconColorId(R.color.on_map_icon_color, 0)
					.setBg(R.drawable.btn_circle_trans_10_new, R.drawable.btn_circle_night_new);
			controls.add(layersHud);
			configureMap.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					MapActivity.clearPrevActivityIntent();
					mapActivity.getDashboard().setDashboardVisibility(true, DashboardType.CONFIGURE_MAP);
				}
			});

			View search = mapActivity.findViewById(R.id.map_search_button);
			search.setVisibility(View.INVISIBLE);
			quickSearchHud = createHudButton(search, R.drawable.map_search_dark)
					.setIconsId(R.drawable.map_search_dark, R.drawable.map_search_night)
					.setIconColorId(0)
					.setBg(R.drawable.btn_circle_trans_10_new, R.drawable.btn_circle_night_new);
			controls.add(quickSearchHud);
			search.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					mapActivity.showQuickSearch(ShowQuickSearchMode.NEW_IF_EXPIRED, false);
				}
			});

			View backToLocation = mapActivity.findViewById(R.id.map_my_location_button);
			backToLocationControl = createHudButton(backToLocation, R.drawable.map_my_location)
					.setBg(R.drawable.btn_circle_blue_new);
			controls.add(backToLocationControl);

			backToLocation.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (OsmAndLocationProvider.isLocationPermissionAvailable(mapActivity)) {
						mapActivity.getMapViewTrackingUtilities().backToLocationImpl();
					} else {
						ActivityCompat.requestPermissions(mapActivity,
								new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
								OsmAndLocationProvider.REQUEST_LOCATION_PERMISSION);
					}
				}
			});
			View backToMenuButton = mapActivity.findViewById(R.id.map_menu_button);

			final boolean dash = settings.SHOW_DASHBOARD_ON_MAP_SCREEN.get();
			menuControl = createHudButton(backToMenuButton,
					!dash ? R.drawable.map_action_settings : R.drawable.map_dashboard).setBg(
					R.drawable.btn_circle_trans_35_new, R.drawable.btn_circle_night_new);
			controls.add(menuControl);
			backToMenuButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					MapActivity.clearPrevActivityIntent();
					Singleton.getInstance().initState = Singleton.state.INIT;
					Intent myIntent = new Intent(mapActivity, SetupActivity.class);
					mapActivity.startActivity(myIntent);
				}
			});
			zoomText = (TextView) mapActivity.findViewById(R.id.map_app_mode_text);

			View routePlanButton = mapActivity.findViewById(R.id.map_route_info_button);
			routePlanningBtn = createHudButton(routePlanButton, R.drawable.map_directions).setBg(
					R.drawable.btn_circle_trans_10_new, R.drawable.btn_circle_night_new);
			controls.add(routePlanningBtn);
			routePlanButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					doRoute(false);
				}
			});
		} else {
			View backToLocation = mapActivity.findViewById(R.id.map_my_location_button);
			backToLocationControl = createHudButton(backToLocation, R.drawable.map_my_location)
					.setBg(R.drawable.btn_circle_blue);
			controls.add(backToLocationControl);

			backToLocation.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (OsmAndLocationProvider.isLocationPermissionAvailable(mapActivity)) {
						mapActivity.getMapViewTrackingUtilities().backToLocationImpl();
					} else {
						ActivityCompat.requestPermissions(mapActivity,
								new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
								OsmAndLocationProvider.REQUEST_LOCATION_PERMISSION);
					}
				}
			});
			View backToMenuButton = mapActivity.findViewById(R.id.map_menu_button);

			final boolean dash = settings.SHOW_DASHBOARD_ON_MAP_SCREEN.get();
			menuControl = createHudButton(backToMenuButton,
					!dash ? R.drawable.map_action_settings : R.drawable.map_dashboard).setBg(
					R.drawable.btn_round, R.drawable.btn_round_night);
			controls.add(menuControl);
			backToMenuButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					MapActivity.clearPrevActivityIntent();
					Singleton.getInstance().initState = Singleton.state.INIT;
					Intent myIntent = new Intent(mapActivity, SetupActivity.class);
					mapActivity.startActivity(myIntent);
				}
			});
			zoomText = (TextView) mapActivity.findViewById(R.id.map_app_mode_text);

			View routePlanButton = mapActivity.findViewById(R.id.map_route_info_button);
			routePlanningBtn = createHudButton(routePlanButton, R.drawable.map_directions).setBg(
					R.drawable.btn_round, R.drawable.btn_round_night);
			controls.add(routePlanningBtn);
			routePlanButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					doRoute(false);
				}
			});
		}

	}

	public void doRoute(boolean hasTargets) {
		this.hasTargets = hasTargets;
		if (OsmAndLocationProvider.isLocationPermissionAvailable(mapActivity)) {
			onNavigationClick();
		} else {
			ActivityCompat.requestPermissions(mapActivity,
					new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
					REQUEST_LOCATION_FOR_NAVIGATION_PERMISSION);
		}
	}

	public void doNavigate() {
		mapRouteInfoMenu.hide();
		startNavigation();
	}

	private void onNavigationClick() {
		MapActivity.clearPrevActivityIntent();
		RoutingHelper routingHelper = mapActivity.getRoutingHelper();
		if (!routingHelper.isFollowingMode() && !routingHelper.isRoutePlanningMode()) {
			if (settings.USE_MAP_MARKERS.get() && !hasTargets) {
				getTargets().restoreTargetPoints(false);
				if (getTargets().getPointToNavigate() == null) {
					mapActivity.getMapActions().setFirstMapMarkerAsTarget();
				}
			}
			TargetPoint start = getTargets().getPointToStart();
			if (start != null) {
				mapActivity.getMapActions().enterRoutePlanningMode(
						new LatLon(start.getLatitude(), start.getLongitude()), start.getOriginalPointDescription());
			} else {
				mapActivity.getMapActions().enterRoutePlanningMode(null, null);
			}
		} else {
			showRouteInfoControlDialog();
		}
		hasTargets = false;
	}

	public void navigateFab() {
		if (!OsmAndLocationProvider.isLocationPermissionAvailable(mapActivity)) {
			ActivityCompat.requestPermissions(mapActivity,
					new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
					REQUEST_LOCATION_FOR_NAVIGATION_FAB_PERMISSION);
		} else {
			final MapContextMenu menu = mapActivity.getContextMenu();
			final LatLon latLon = menu.getLatLon();
			final PointDescription pointDescription = menu.getPointDescriptionForTarget();
			menu.hide();
			final TargetPointsHelper targets = mapActivity.getMyApplication().getTargetPointsHelper();
			RoutingHelper routingHelper = mapActivity.getMyApplication().getRoutingHelper();
			if (routingHelper.isFollowingMode() || routingHelper.isRoutePlanningMode()) {
				DirectionsDialogs.addWaypointDialogAndLaunchMap(mapActivity, latLon.getLatitude(),
						latLon.getLongitude(), pointDescription);
			} else if (targets.getIntermediatePoints().isEmpty()) {
				startRoutePlanningWithDestination(latLon, pointDescription, targets);
				menu.close();
			} else {
				AlertDialog.Builder bld = new AlertDialog.Builder(mapActivity);
				bld.setTitle(R.string.new_directions_point_dialog);
				final int[] defaultVls = new int[]{0};
				bld.setSingleChoiceItems(new String[]{
						mapActivity.getString(R.string.clear_intermediate_points),
						mapActivity.getString(R.string.keep_intermediate_points)
				}, 0, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						defaultVls[0] = which;
					}
				});
				bld.setPositiveButton(R.string.shared_string_ok, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (defaultVls[0] == 0) {
							targets.removeAllWayPoints(false, true);
							targets.navigateToPoint(latLon, true, -1, pointDescription);
							mapActivity.getMapActions().enterRoutePlanningModeGivenGpx(null, null, null, true, true);
							menu.close();
						} else {
							targets.navigateToPoint(latLon, true, -1, pointDescription);
							mapActivity.getMapActions().enterRoutePlanningModeGivenGpx(null, null, null, true, true);
							menu.close();
						}
					}
				});
				bld.setNegativeButton(R.string.shared_string_cancel, null);
				bld.show();
			}
		}
	}

	private void startRoutePlanningWithDestination(LatLon latLon, PointDescription pointDescription, TargetPointsHelper targets) {
		boolean hasPointToStart = settings.restorePointToStart();
		targets.navigateToPoint(latLon, true, -1, pointDescription);
		if (!hasPointToStart) {
			mapActivity.getMapActions().enterRoutePlanningModeGivenGpx(null, null, null, true, true);
		} else {
			TargetPoint start = targets.getPointToStart();
			if (start != null) {
				mapActivity.getMapActions().enterRoutePlanningModeGivenGpx(null, start.point, start.getOriginalPointDescription(), true, true);
			} else {
				mapActivity.getMapActions().enterRoutePlanningModeGivenGpx(null, null, null, true, true);
			}
		}
	}

	private PointDescription getPointDescriptionForTarget(LatLon latLon) {
		final MapContextMenu menu = mapActivity.getContextMenu();
		PointDescription pointDescription;
		if (menu.isActive() && latLon.equals(menu.getLatLon())) {
			pointDescription = menu.getPointDescriptionForTarget();
		} else {
			pointDescription = new PointDescription(PointDescription.POINT_TYPE_LOCATION, "");
		}
		return pointDescription;
	}

	public void addDestination(LatLon latLon) {
		if (latLon != null) {
			if (!OsmAndLocationProvider.isLocationPermissionAvailable(mapActivity)) {
				requestedLatLon = latLon;
				ActivityCompat.requestPermissions(mapActivity,
						new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
						REQUEST_LOCATION_FOR_ADD_DESTINATION_PERMISSION);
			} else {
				PointDescription pointDescription = getPointDescriptionForTarget(latLon);
				mapActivity.getContextMenu().close();
				final TargetPointsHelper targets = mapActivity.getMyApplication().getTargetPointsHelper();
				RoutingHelper routingHelper = mapActivity.getMyApplication().getRoutingHelper();
				if (routingHelper.isFollowingMode() || routingHelper.isRoutePlanningMode()) {
					targets.navigateToPoint(latLon, true, targets.getIntermediatePoints().size() + 1, pointDescription);
				} else if (targets.getIntermediatePoints().isEmpty()) {
					startRoutePlanningWithDestination(latLon, pointDescription, targets);
				}
			}
		}
	}

	public void addFirstIntermediate(LatLon latLon) {
		if (latLon != null) {
			RoutingHelper routingHelper = mapActivity.getMyApplication().getRoutingHelper();
			if (routingHelper.isFollowingMode() || routingHelper.isRoutePlanningMode()) {
				PointDescription pointDescription = getPointDescriptionForTarget(latLon);
				mapActivity.getContextMenu().close();
				final TargetPointsHelper targets = mapActivity.getMyApplication().getTargetPointsHelper();
				if (routingHelper.isFollowingMode() || routingHelper.isRoutePlanningMode()) {
					targets.navigateToPoint(latLon, true, 0, pointDescription);
				} else if (targets.getIntermediatePoints().isEmpty()) {
					startRoutePlanningWithDestination(latLon, pointDescription, targets);
				}
			} else {
				addDestination(latLon);
			}
		}
	}

	public void replaceDestination(LatLon latLon) {
		RoutingHelper routingHelper = mapActivity.getMyApplication().getRoutingHelper();
		if (latLon != null) {
			if (routingHelper.isFollowingMode() || routingHelper.isRoutePlanningMode()) {
				PointDescription pointDescription = getPointDescriptionForTarget(latLon);
				mapActivity.getContextMenu().close();
				final TargetPointsHelper targets = mapActivity.getMyApplication().getTargetPointsHelper();
				targets.navigateToPoint(latLon, true, -1, pointDescription);
			} else {
				addDestination(latLon);
			}
		}
	}

	public void switchToRouteFollowingLayout() {
		touchEvent = 0;
		mapActivity.getMyApplication().getRoutingHelper().setRoutePlanningMode(false);
		mapActivity.getMapViewTrackingUtilities().switchToRoutePlanningMode();
		mapActivity.refreshMap();
	}

	public boolean switchToRoutePlanningLayout() {
		if (!mapActivity.getRoutingHelper().isRoutePlanningMode() && mapActivity.getRoutingHelper().isFollowingMode()) {
			mapActivity.getRoutingHelper().setRoutePlanningMode(true);
			mapActivity.getMapViewTrackingUtilities().switchToRoutePlanningMode();
			mapActivity.refreshMap();
			return true;
		}
		return false;
	}

	private void initZooms() {
		if (settings.NEW_MAP_VIEW.get()) {
			final OsmandMapTileView view = mapActivity.getMapView();
			final View zoomInButton = mapActivity.findViewById(R.id.map_zoom_in_button);
			mapZoomIn = createHudButton(zoomInButton, R.drawable.list_destination).
					setIconsId(R.drawable.list_destination, R.drawable.list_destination);
			controls.add(mapZoomIn);
			zoomInButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					PopupMenu popup = new PopupMenu(mapActivity.getApplicationContext(), zoomInButton);
					popup.getMenuInflater().inflate(R.menu.setendpopup, popup.getMenu());
					popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
						public boolean onMenuItemClick(MenuItem item) {
							switch (item.getTitle().toString()) {
                                case "Choose End on map":
                                    Singleton.getInstance().setChooseEnd(true);
									Singleton.getInstance().setEndToCurrentPosition(false);
                                    Singleton.getInstance().initState = Singleton.state.INIT2;
                                    break;
								case "Use current location":
									Singleton.getInstance().setEndToCurrentPosition(true);
                                    Singleton.getInstance().initState = Singleton.state.INIT2;
									break;
								case "Same Start and End":
									Singleton.getInstance().setChooseEnd(false);
									Singleton.getInstance().setEndToCurrentPosition(false);
									break;
                            }
							mapActivity.runInitEnd();
							return true;
						}
					});
					popup.show();
				}
			});
			final View.OnLongClickListener listener = MapControlsLayer.getOnClickMagnifierListener(view);
			zoomInButton.setOnLongClickListener(listener);
			final View zoomOutButton = mapActivity.findViewById(R.id.map_zoom_out_button);
			mapZoomOut = createHudButton(zoomOutButton, R.drawable.list_startpoint).
					setIconsId(R.drawable.list_startpoint, R.drawable.list_startpoint);
			controls.add(mapZoomOut);
			zoomOutButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					PopupMenu popup = new PopupMenu(mapActivity.getApplicationContext(), zoomOutButton);
					popup.getMenuInflater().inflate(R.menu.setstartpopup, popup.getMenu());
					popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
						public boolean onMenuItemClick(MenuItem item) {
							switch (item.getTitle().toString()) {
								case "Choose Start on map":
									Singleton.getInstance().setChooseStart(true);
									Singleton.getInstance().setStartToCurrentPosition(false);
									Singleton.getInstance().initState = Singleton.state.INIT;
									break;
								case "Use current location":
									Singleton.getInstance().setStartToCurrentPosition(true);
                                    Singleton.getInstance().initState = Singleton.state.INIT;
									break;
								case "Same Start and End":
									Singleton.getInstance().setChooseStart(false);
									Singleton.getInstance().setStartToCurrentPosition(false);
									break;
							}
							mapActivity.runInitStart();
							return true;
						}
					});
					popup.show();
				}
			});
			zoomOutButton.setOnLongClickListener(listener);
		} else {
			final OsmandMapTileView view = mapActivity.getMapView();
			final View zoomInButton = mapActivity.findViewById(R.id.map_zoom_in_button);
			mapZoomIn = createHudButton(zoomInButton, R.drawable.list_destination).
					setIconsId(R.drawable.list_destination, R.drawable.list_destination);
			controls.add(mapZoomIn);
			zoomInButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
                    PopupMenu popup = new PopupMenu(mapActivity.getApplicationContext(), zoomInButton);
                    popup.getMenuInflater().inflate(R.menu.setendpopup, popup.getMenu());
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getTitle().toString()) {
                                case "Choose End on map":
                                    Singleton.getInstance().setChooseEnd(true);
                                    Singleton.getInstance().setEndToCurrentPosition(false);
                                    Singleton.getInstance().initState = Singleton.state.INIT2;
                                    break;
                                case "Use current location":
                                    Singleton.getInstance().setEndToCurrentPosition(true);
                                    Singleton.getInstance().initState = Singleton.state.INIT2;
                                    break;
                                case "Same Start and End":
                                    Singleton.getInstance().setChooseEnd(false);
                                    Singleton.getInstance().setEndToCurrentPosition(false);
                                    break;
                            }
                            mapActivity.runInitEnd();
                            return true;
                        }
                    });
                    popup.show();
				}
			});
			final View.OnLongClickListener listener = MapControlsLayer.getOnClickMagnifierListener(view);
			zoomInButton.setOnLongClickListener(listener);
			final View zoomOutButton = mapActivity.findViewById(R.id.map_zoom_out_button);
			mapZoomOut = createHudButton(zoomOutButton, R.drawable.list_startpoint).
					setIconsId(R.drawable.list_startpoint, R.drawable.list_startpoint);
			controls.add(mapZoomOut);
			zoomOutButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
                    PopupMenu popup = new PopupMenu(mapActivity.getApplicationContext(), zoomOutButton);
                    popup.getMenuInflater().inflate(R.menu.setstartpopup, popup.getMenu());
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getTitle().toString()) {
                                case "Choose Start on map":
                                    Singleton.getInstance().setChooseStart(true);
                                    Singleton.getInstance().setStartToCurrentPosition(false);
                                    Singleton.getInstance().initState = Singleton.state.INIT;
                                    break;
                                case "Use current location":
                                    Singleton.getInstance().setStartToCurrentPosition(true);
                                    Singleton.getInstance().initState = Singleton.state.INIT;
                                    break;
                                case "Same Start and End":
                                    Singleton.getInstance().setChooseStart(false);
                                    Singleton.getInstance().setStartToCurrentPosition(false);
                                    break;
                            }
                            mapActivity.runInitStart();
                            return true;
                        }
                    });
                    popup.show();
				}
			});
			zoomOutButton.setOnLongClickListener(listener);
		}
	}

	public void startNavigation() {
		OsmandApplication app = mapActivity.getMyApplication();
		RoutingHelper routingHelper = app.getRoutingHelper();
		if (routingHelper.isFollowingMode()) {
			switchToRouteFollowingLayout();
			if (app.getSettings().APPLICATION_MODE.get() != routingHelper.getAppMode()) {
				app.getSettings().APPLICATION_MODE.set(routingHelper.getAppMode());
			}
		} else {
			if (!app.getTargetPointsHelper().checkPointToNavigateShort()) {
				mapRouteInfoMenu.show();
			} else {
				touchEvent = 0;
				app.logEvent(mapActivity, "start_navigation");
				app.getSettings().APPLICATION_MODE.set(routingHelper.getAppMode());
				mapActivity.getMapViewTrackingUtilities().backToLocationImpl();
				app.getSettings().FOLLOW_THE_ROUTE.set(true);
				routingHelper.setFollowingMode(true);
				routingHelper.setRoutePlanningMode(false);
				mapActivity.getMapViewTrackingUtilities().switchToRoutePlanningMode();
				app.getRoutingHelper().notifyIfRouteIsCalculated();
				routingHelper.setCurrentLocation(app.getLocationProvider().getLastKnownLocation(), false);
			}
		}
	}

	@Override
	public void destroyLayer() {
		controls.clear();
	}

	@Override
	public void onDraw(Canvas canvas, RotatedTileBox tileBox, DrawSettings nightMode) {
		updateControls(tileBox, nightMode);
	}

	@SuppressWarnings("deprecation")
	private void updateControls(@NonNull RotatedTileBox tileBox, DrawSettings drawSettings) {
		boolean isNight = drawSettings != null && drawSettings.isNightMode();
		int shadw = isNight ? Color.TRANSPARENT : Color.WHITE;
		int textColor = isNight ? mapActivity.getResources().getColor(R.color.widgettext_night) : Color.BLACK;
		if (shadowColor != shadw) {
			shadowColor = shadw;
			// TODOnightMode
			// updatextColor(textColor, shadw, rulerControl, zoomControls, mapMenuControls);
		}
		// default buttons
		boolean routePlanningMode = false;
		RoutingHelper rh = mapActivity.getRoutingHelper();
		if (rh.isRoutePlanningMode()) {
			routePlanningMode = true;
		} else if ((rh.isRouteCalculated() || rh.isRouteBeingCalculated()) && !rh.isFollowingMode()) {
			routePlanningMode = true;
		}
		boolean routeFollowingMode = !routePlanningMode && rh.isFollowingMode();
		boolean routeDialogOpened = MapRouteInfoMenu.isVisible();
		boolean trackDialogOpened = TrackDetailsMenu.isVisible();
		boolean showRouteCalculationControls = routePlanningMode ||
				((app.accessibilityEnabled() || (System.currentTimeMillis() - touchEvent < TIMEOUT_TO_SHOW_BUTTONS)) && routeFollowingMode);
		updateMyLocation(rh, routeDialogOpened || trackDialogOpened);
		boolean showButtons = true;
		//routePlanningBtn.setIconResId(routeFollowingMode ? R.drawable.ic_action_gabout_dark : R.drawable.map_directions);
		if (rh.isFollowingMode()) {
			routePlanningBtn.setIconResId(R.drawable.map_start_navigation);
			routePlanningBtn.setIconColorId(R.color.color_myloc_distance);
		} else if (routePlanningMode) {
			routePlanningBtn.setIconResId(R.drawable.map_directions);
			routePlanningBtn.setIconColorId(R.color.color_myloc_distance);
		} else {
			routePlanningBtn.setIconResId(R.drawable.map_directions);
			routePlanningBtn.resetIconColors();
		}
		routePlanningBtn.updateVisibility(showButtons);
		menuControl.updateVisibility(showButtons);

		mapZoomIn.updateVisibility(!routeDialogOpened);
		mapZoomOut.updateVisibility(!routeDialogOpened);
		compassHud.updateVisibility(!routeDialogOpened && !trackDialogOpened && shouldShowCompass());

		if (layersHud.setIconResId(settings.getApplicationMode().getMapIconId())) {
			layersHud.update(app, isNight);
		}
		layersHud.updateVisibility(false);
		quickSearchHud.updateVisibility(false);

		if (!routePlanningMode && !routeFollowingMode) {
			if (mapView.isZooming()) {
				lastZoom = System.currentTimeMillis();
			}
			//if (!mapView.isZooming() || !OsmandPlugin.isDevelopment()) {
			if ((System.currentTimeMillis() - lastZoom > 1000) || !OsmandPlugin.isDevelopment()) {
				zoomText.setVisibility(View.GONE);
			} else {
				zoomText.setVisibility(View.VISIBLE);
				zoomText.setTextColor(textColor);
				zoomText.setText(getZoomLevel(tileBox));
			}
		}

		mapRouteInfoMenu.setVisible(showRouteCalculationControls);
		updateCompass(isNight);

		for (MapHudButton mc : controls) {
			mc.update(mapActivity.getMyApplication(), isNight);
		}
	}

	public void updateCompass(boolean isNight) {
		float mapRotate = mapActivity.getMapView().getRotate();
		boolean showCompass = shouldShowCompass();
		if (mapRotate != cachedRotate) {
			cachedRotate = mapRotate;
			// Apply animation to image view
			compassHud.iv.invalidate();
			compassHud.updateVisibility(showCompass);
		}
		if (settings.ROTATE_MAP.get() == OsmandSettings.ROTATE_MAP_NONE) {
			compassHud.setIconResId(isNight ? R.drawable.map_compass_niu_white : R.drawable.map_compass_niu);
			compassHud.iv.setContentDescription(mapActivity.getString(R.string.rotate_map_none_opt));
			compassHud.updateVisibility(showCompass);
		} else if (settings.ROTATE_MAP.get() == OsmandSettings.ROTATE_MAP_BEARING) {
			compassHud.setIconResId(isNight ? R.drawable.map_compass_bearing_white : R.drawable.map_compass_bearing);
			compassHud.iv.setContentDescription(mapActivity.getString(R.string.rotate_map_bearing_opt));
			compassHud.updateVisibility(true);
		} else {
			compassHud.setIconResId(isNight ? R.drawable.map_compass_white : R.drawable.map_compass);
			compassHud.iv.setContentDescription(mapActivity.getString(R.string.rotate_map_compass_opt));
			compassHud.updateVisibility(true);
		}
	}

	private boolean shouldShowCompass() {
		float mapRotate = mapActivity.getMapView().getRotate();
		return forceShowCompass || mapRotate != 0
					|| settings.ROTATE_MAP.get() != OsmandSettings.ROTATE_MAP_NONE
					|| mapActivity.getMapLayers().getMapInfoLayer().getMapInfoControls().isVisible("compass");
	}

	public CompassDrawable getCompassDrawable(Drawable originalDrawable) {
		return new CompassDrawable(originalDrawable);
	}

	private void updateMyLocation(RoutingHelper rh, boolean dialogOpened) {
		boolean enabled = mapActivity.getMyApplication().getLocationProvider().getLastKnownLocation() != null;
		boolean tracked = mapActivity.getMapViewTrackingUtilities().isMapLinkedToLocation();

		if (settings.NEW_MAP_VIEW.get()) {
			if (!enabled) {
				backToLocationControl.setBg(R.drawable.btn_circle_trans_10_new, R.drawable.btn_circle_night_new);
				backToLocationControl.setIconColorId(R.color.icon_color, 0);
				backToLocationControl.iv.setContentDescription(mapActivity.getString(R.string.unknown_location));
			} else if (tracked) {
				backToLocationControl.setBg(R.drawable.btn_circle_trans_10_new, R.drawable.btn_circle_night_new);
				backToLocationControl.setIconColorId(R.color.color_myloc_distance);
				backToLocationControl.iv.setContentDescription(mapActivity.getString(R.string.access_map_linked_to_location));
			} else {
				backToLocationControl.setIconColorId(0);
				backToLocationControl.setBg(R.drawable.btn_circle_blue_new);
				backToLocationControl.iv.setContentDescription(mapActivity.getString(R.string.map_widget_back_to_loc));
			}
			boolean visible = !(tracked && rh.isFollowingMode());
			backToLocationControl.updateVisibility(visible && !dialogOpened);
			if (app.accessibilityEnabled()) {
				backToLocationControl.iv.setClickable(enabled && visible);
			}
		} else {
			if (!enabled) {
				backToLocationControl.setBg(R.drawable.btn_circle, R.drawable.btn_circle_night);
				backToLocationControl.setIconColorId(R.color.icon_color, 0);
				backToLocationControl.iv.setContentDescription(mapActivity.getString(R.string.unknown_location));
			} else if (tracked) {
				backToLocationControl.setBg(R.drawable.btn_circle, R.drawable.btn_circle_night);
				backToLocationControl.setIconColorId(R.color.color_myloc_distance);
				backToLocationControl.iv.setContentDescription(mapActivity.getString(R.string.access_map_linked_to_location));
			} else {
				backToLocationControl.setIconColorId(0);
				backToLocationControl.setBg(R.drawable.btn_circle_blue);
				backToLocationControl.iv.setContentDescription(mapActivity.getString(R.string.map_widget_back_to_loc));
			}
			boolean visible = !(tracked && rh.isFollowingMode());
			backToLocationControl.updateVisibility(visible && !dialogOpened);
			if (app.accessibilityEnabled()) {
				backToLocationControl.iv.setClickable(enabled && visible);
			}
		}
	}


	public boolean onSingleTap(PointF point, RotatedTileBox tileBox) {
		return mapRouteInfoMenu.onSingleTap(point, tileBox);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event, RotatedTileBox tileBox) {
		touchEvent = System.currentTimeMillis();
		RoutingHelper rh = mapActivity.getRoutingHelper();
		if (rh.isFollowingMode()) {
			mapActivity.refreshMap();
		}
		return false;
	}

	// /////////////// Transparency bar /////////////////////////
	private void initTransparencyBar() {
		transparencyBarLayout = (LinearLayout) mapActivity.findViewById(R.id.map_transparency_layout);
		transparencyBar = (SeekBar) mapActivity.findViewById(R.id.map_transparency_seekbar);
		transparencyBar.setMax(255);
		if (transparencySetting != null) {
			transparencyBar.setProgress(transparencySetting.get());
			transparencyBarLayout.setVisibility(View.VISIBLE);
		} else {
			transparencyBarLayout.setVisibility(View.GONE);
		}
		transparencyBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if (transparencySetting != null) {
					transparencySetting.set(progress);
					mapActivity.getMapView().refreshMap();
				}
			}
		});
		ImageButton imageButton = (ImageButton) mapActivity.findViewById(R.id.map_transparency_hide);
		imageButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				transparencyBarLayout.setVisibility(View.GONE);
				settings.LAYER_TRANSPARENCY_SEEKBAR_MODE.set(LayerTransparencySeekbarMode.OFF);
				hideTransparencyBar(transparencySetting);
			}
		});

		LayerTransparencySeekbarMode seekbarMode = settings.LAYER_TRANSPARENCY_SEEKBAR_MODE.get();
		if (seekbarMode == LayerTransparencySeekbarMode.OVERLAY && settings.MAP_OVERLAY.get() != null) {
			showTransparencyBar(settings.MAP_OVERLAY_TRANSPARENCY);
			setTransparencyBarEnabled(true);
		} else if (seekbarMode == LayerTransparencySeekbarMode.UNDERLAY && settings.MAP_UNDERLAY.get() != null) {
			showTransparencyBar(settings.MAP_TRANSPARENCY);
			setTransparencyBarEnabled(true);
		}
	}

	public void showTransparencyBar(CommonPreference<Integer> transparenPreference) {
		if (MapControlsLayer.transparencySetting != transparenPreference) {
			MapControlsLayer.transparencySetting = transparenPreference;
			if (isTransparencyBarEnabled) {
				transparencyBarLayout.setVisibility(View.VISIBLE);
			}
			transparencyBar.setProgress(transparenPreference.get());
		}
	}

	public void hideTransparencyBar(CommonPreference<Integer> transparentPreference) {
		if (transparencySetting == transparentPreference) {
			transparencyBarLayout.setVisibility(View.GONE);
			transparencySetting = null;
		}
	}

	public void setTransparencyBarEnabled(boolean isTransparencyBarEnabled) {
		this.isTransparencyBarEnabled = isTransparencyBarEnabled;
		if (transparencySetting != null) {
			if (isTransparencyBarEnabled) {
				transparencyBarLayout.setVisibility(View.VISIBLE);
			} else {
				transparencyBarLayout.setVisibility(View.GONE);
			}
		}
	}

	private class MapHudButton {
		View iv;
		int bgDark;
		int bgLight;
		int resId;
		int resLightId;
		int resDarkId;
		int resClrLight = R.color.icon_color;
		int resClrDark = 0;

		boolean nightMode = false;
		boolean f = true;
		boolean compass;
		boolean compassOutside;
		ViewPropertyAnimatorCompat hideAnimator;

		public MapHudButton setRoundTransparent() {
			setBg(R.drawable.btn_circle_trans, R.drawable.btn_circle_night);
			return this;
		}


		public MapHudButton setBg(int dayBg, int nightBg) {
			if (bgDark == nightBg && dayBg == bgLight) {
				return this;
			}
			bgDark = nightBg;
			bgLight = dayBg;
			f = true;
			return this;
		}

		public void hideDelayed(long msec) {
			if (!compassOutside && (iv.getVisibility() == View.VISIBLE)) {
				if (hideAnimator != null) {
					hideAnimator.cancel();
				}
				hideAnimator = ViewCompat.animate(iv).alpha(0f).setDuration(250).setStartDelay(msec).setListener(new ViewPropertyAnimatorListener() {
					@Override
					public void onAnimationStart(View view) {
					}

					@Override
					public void onAnimationEnd(View view) {
						iv.setVisibility(View.GONE);
						ViewCompat.setAlpha(iv, 1f);
						hideAnimator = null;
					}

					@Override
					public void onAnimationCancel(View view) {
						iv.setVisibility(View.GONE);
						ViewCompat.setAlpha(iv, 1f);
						hideAnimator = null;
					}
				});
				hideAnimator.start();
			}
		}

		public void cancelHideAnimation() {
			if (hideAnimator != null) {
				hideAnimator.cancel();
			}
		}

		public boolean updateVisibility(boolean visible) {
			if (!compassOutside && visible != (iv.getVisibility() == View.VISIBLE)) {
				if (visible) {
					if (hideAnimator != null) {
						hideAnimator.cancel();
					}
					iv.setVisibility(View.VISIBLE);
					iv.invalidate();
				} else if (hideAnimator == null) {
					if (compass) {
						hideDelayed(5000);
					} else {
						iv.setVisibility(View.GONE);
						iv.invalidate();
					}
				}
				return true;
			} else if (visible && hideAnimator != null) {
				hideAnimator.cancel();
				iv.setVisibility(View.VISIBLE);
				iv.invalidate();
			}
			return false;
		}

		public MapHudButton setBg(int bg) {
			if (bgDark == bg && bg == bgLight) {
				return this;
			}
			bgDark = bg;
			bgLight = bg;
			f = true;
			return this;
		}

		public boolean setIconResId(int resId) {
			if (this.resId == resId) {
				return false;
			}
			this.resId = resId;
			f = true;
			return true;
		}

		public boolean resetIconColors() {
			if (resClrLight == R.color.icon_color && resClrDark == 0) {
				return false;
			}
			resClrLight = R.color.icon_color;
			resClrDark = 0;
			f = true;
			return true;
		}

		public MapHudButton setIconColorId(int clr) {
			if (resClrLight == clr && resClrDark == clr) {
				return this;
			}
			resClrLight = clr;
			resClrDark = clr;
			f = true;
			return this;
		}

		public MapHudButton setIconsId(int icnLight, int icnDark) {
			if (resLightId == icnLight && resDarkId == icnDark) {
				return this;
			}
			resLightId = icnLight;
			resDarkId = icnDark;
			f = true;
			return this;
		}

		public MapHudButton setIconColorId(int clrLight, int clrDark) {
			if (resClrLight == clrLight && resClrDark == clrDark) {
				return this;
			}
			resClrLight = clrLight;
			resClrDark = clrDark;
			f = true;
			return this;
		}

		@SuppressLint("NewApi")
		@SuppressWarnings("deprecation")
		public void update(OsmandApplication ctx, boolean night) {
			if (nightMode == night && !f) {
				return;
			}
			f = false;
			nightMode = night;
			if (bgDark != 0 && bgLight != 0) {
				if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
					iv.setBackground(ctx.getResources().getDrawable(night ? bgDark : bgLight,
							mapActivity.getTheme()));
				} else {
					iv.setBackgroundDrawable(ctx.getResources().getDrawable(night ? bgDark : bgLight));
				}
			}
			Drawable d = null;
			if (resDarkId != 0 && nightMode) {
				d = ctx.getIconsCache().getIcon(resDarkId);
			} else if (resLightId != 0 && !nightMode) {
				d = ctx.getIconsCache().getIcon(resLightId);
			} else if (resId != 0) {
				d = ctx.getIconsCache().getIcon(resId, nightMode ? resClrDark : resClrLight);
			}

			if (iv instanceof ImageView) {
				if (compass) {
					((ImageView) iv).setImageDrawable(new CompassDrawable(d));
				} else {
					((ImageView) iv).setImageDrawable(d);
				}
			} else if (iv instanceof TextView) {
				((TextView) iv).setCompoundDrawablesWithIntrinsicBounds(
						d, null, null, null);
			}
		}

	}

	private String getZoomLevel(@NonNull RotatedTileBox tb) {
		String zoomText = tb.getZoom() + "";
		double frac = tb.getMapDensity();
		if (frac != 0) {
			int ifrac = (int) (frac * 10);
			zoomText += " ";
			zoomText += Math.abs(ifrac) / 10;
			if (ifrac % 10 != 0) {
				zoomText += "." + Math.abs(ifrac) % 10;
			}
		}
		return zoomText;
	}

	public void setMapQuickActionLayer(MapQuickActionLayer mapQuickActionLayer) {
		this.mapQuickActionLayer = mapQuickActionLayer;
	}

	private boolean isInChangeMarkerPositionMode(){
		return mapQuickActionLayer == null ? contextMenuLayer.isInChangeMarkerPositionMode() :
				mapQuickActionLayer.isInChangeMarkerPositionMode() || contextMenuLayer.isInChangeMarkerPositionMode();
	}

	private boolean isInGpxDetailsMode() {
		return contextMenuLayer.isInGpxDetailsMode();
	}

	public static View.OnLongClickListener getOnClickMagnifierListener(final OsmandMapTileView view) {
		return new View.OnLongClickListener() {

			@Override
			public boolean onLongClick(View notUseCouldBeNull) {
				final OsmandSettings.OsmandPreference<Float> mapDensity = view.getSettings().MAP_DENSITY;
				final AlertDialog.Builder bld = new AlertDialog.Builder(view.getContext());
				int p = (int) (mapDensity.get() * 100);
				final TIntArrayList tlist = new TIntArrayList(new int[]{20, 25, 33, 50, 75, 100, 150, 200, 300, 400});
				final List<String> values = new ArrayList<>();
				int i = -1;
				for (int k = 0; k <= tlist.size(); k++) {
					final boolean end = k == tlist.size();
					if (i == -1) {
						if ((end || p < tlist.get(k))) {
							values.add(p + " %");
							i = k;
						} else if (p == tlist.get(k)) {
							i = k;
						}

					}
					if (k < tlist.size()) {
						values.add(tlist.get(k) + " %");
					}
				}
				if (values.size() != tlist.size()) {
					tlist.insert(i, p);
				}

				bld.setTitle(R.string.map_magnifier);
				bld.setSingleChoiceItems(values.toArray(new String[values.size()]), i,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								int p = tlist.get(which);
								mapDensity.set(p / 100.0f);
								view.setComplexZoom(view.getZoom(), view.getSettingsMapDensity());
								MapRendererContext mapContext = NativeCoreContext.getMapRendererContext();
								if (mapContext != null) {
									mapContext.updateMapSettings();
								}
								dialog.dismiss();
							}
						});
				bld.show();
				return true;
			}
		};
	}

	public void selectAddress(String name, double latitude, double longitude, boolean target) {
		if (name != null) {
			mapRouteInfoMenu.selectAddress(name, new LatLon(latitude, longitude), target);
		} else {
			mapRouteInfoMenu.selectAddress("", new LatLon(latitude, longitude), target);
		}
	}

	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		if (requestCode == REQUEST_LOCATION_FOR_NAVIGATION_PERMISSION
				&& grantResults[0] == PackageManager.PERMISSION_GRANTED) {
			onNavigationClick();
		} else if (requestCode == REQUEST_LOCATION_FOR_NAVIGATION_FAB_PERMISSION
				&& grantResults[0] == PackageManager.PERMISSION_GRANTED) {
			navigateFab();
		} else if (requestCode == REQUEST_LOCATION_FOR_ADD_DESTINATION_PERMISSION
				&& grantResults[0] == PackageManager.PERMISSION_GRANTED) {
			addDestination(requestedLatLon);
		}
	}
}
