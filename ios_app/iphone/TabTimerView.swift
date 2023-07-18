import SwiftUI
import shared

private let emojiWidth = 58.0
private let activitiesInnerHPadding = 12.0

struct TabTimerView: View {

    @State private var vm = TabTimerVM()

    @State private var isReadmePresented = false

    @State private var isEditActivitiesPresented = false
    @State private var isSettingsSheetPresented = false
    @State private var isSummaryPresented = false
    @State private var isHistoryPresented = false

    var body: some View {

        // Without NavigationView the NavigationLink does not work,
        // remember the .navigationBarHidden(true)
        NavigationView {

            // If outside of the NavigationView - deletion swipe does not opened in full size.
            VMView(vm: vm) { state in

                ZStack {

                    Color(.bg).edgesIgnoringSafeArea(.all)

                    //
                    // Top Menu Bar

                    VStack {

                        HStack(spacing: 16) {

                            MenuButton(sfName: "line.3.horizontal") { isSettingsSheetPresented.toggle() }

                            Spacer()

                            MenuButton(sfName: "chart.pie") { isSummaryPresented.toggle() }
                                    .sheetEnv(isPresented: $isSummaryPresented) {
                                        VStack {

                                            ChartView()
                                                    .padding(.top, 15)

                                            Button(
                                                    action: { isSummaryPresented.toggle() },
                                                    label: { Text("close").fontWeight(.light) }
                                            )
                                                    .padding(.bottom, 4)
                                        }
                                    }

                            MenuButton(sfName: "list.bullet") { isHistoryPresented.toggle() }
                                    .sheetEnv(isPresented: $isHistoryPresented) {
                                        ZStack {
                                            Color(.myBackground).edgesIgnoringSafeArea(.all)
                                            HistoryView(isHistoryPresented: $isHistoryPresented)
                                        }
                                                // todo
                                                .interactiveDismissDisabled()
                                    }

                            MenuButton(sfName: "square.and.pencil") { isEditActivitiesPresented.toggle() }
                        }
                                .padding(.leading, 15)
                                .padding(.trailing, 14)
                                .padding(.top, 8)
                                .padding(.bottom, 8)

                        //
                        // List

                        ScrollView(.vertical, showsIndicators: false) {

                            VStack {

                                MySpacerSize(height: 21)

                                VStack {

                                    let activitiesUI = state.activitiesUI

                                    ForEach(activitiesUI, id: \.activity.id) { activityUI in
                                        ActivityRowView(
                                                vm: vm,
                                                activityUI: activityUI,
                                                lastInterval: state.lastInterval,
                                                withTopDivider: activityUI.withTopDivider
                                        )
                                    }
                                }
                                /*
                                    .onDelete { set in
                                        // to show the button
                                    }
                                    .onMove { set, toIdx in
                                        if (set.count != 1) {
                                            fatalError("bad count for moving \(set.count)")
                                        }

                                        var tempList = activities.map {
                                            $0
                                        }

                                        let fromIdx = set.first!
                                        if fromIdx < toIdx {
                                            tempList.insert(tempList[fromIdx], at: toIdx)
                                            tempList.remove(at: fromIdx)
                                        } else {
                                            let removedTask = tempList.remove(at: fromIdx)
                                            tempList.insert(removedTask, at: toIdx)
                                        }

                                        for (tempIndex, tempTask) in tempList.enumerated() {
                                            tempTask.updateSort(tempIndex)
                                        }
                                    }
                                     */

                                MySpacerSize(height: 21)
                            }
                        }
                    }
                }
                        .sheetEnv(
                                isPresented: $isEditActivitiesPresented
                        ) {
                            EditActivitiesSheet(
                                    isPresented: $isEditActivitiesPresented
                            )
                        }
                        .sheetEnv(
                                isPresented: $isSettingsSheetPresented
                        ) {
                            SettingsSheet(isPresented: $isSettingsSheetPresented)
                        }
                        .sheetEnv(isPresented: $isReadmePresented) {
                            TabReadmeView(isPresented: $isReadmePresented)
                        }
                        .navigationBarHidden(true)
            }
        }
    }
}

private struct ActivityRowView: View {

    var vm: TabTimerVM
    var activityUI: TabTimerVM.ActivityUI
    var lastInterval: IntervalModel
    var withTopDivider: Bool

    @State private var isSetTimerPresented = false
    @State private var isEditSheetPresented = false

    @State private var isActiveAnim = false
    @State private var bgColorAnim = Color(.bg)

    @EnvironmentObject private var timetoSheet: TimetoSheet

    var body: some View {
        MyListSwipeToActionItem(
                deletionHint: activityUI.deletionHint,
                deletionConfirmationNote: activityUI.deletionConfirmation,
                onEdit: {
                    isEditSheetPresented = true
                },
                onDelete: {
                    activityUI.delete()
                }
        ) {
            ZStack(alignment: .top) {
                AnyView(safeView)
                if withTopDivider {
                    DividerBg()
                            .padding(.leading, emojiWidth)
                }
            }
                    // todo remove after removing MyListSwipeToActionItem()
                    .background(Color(.bg))
        }
                .animateVmValue(value: activityUI.data.timerData != nil, state: $isActiveAnim)
                .animateVmValue(value: activityUI.data.timerData?.color.toColor() ?? Color(.bg), state: $bgColorAnim)
    }

    private var safeView: some View {

        Button(
                action: {
                    timetoSheet.showActivityTimerSheet(
                            activity: activityUI.activity,
                            isPresented: $isSetTimerPresented,
                            timerContext: nil,
                            onStart: {
                                isSetTimerPresented.toggle()
                                /// With animation twitching emoji
                            }
                    )
                },
                label: {

                    VStack(alignment: .leading) {

                        HStack {

                            Text(activityUI.activity.emoji)
                                    .frame(width: emojiWidth)
                                    .font(.system(size: 25))
                                    .animation(nil, value: isActiveAnim)
                                    .shadow(color: .white.opacity(0.5), radius: 1)

                            VStack {

                                HStack {

                                    let textFontSize = isActiveAnim ? 18.0 : 17.0

                                    Text(activityUI.data.text)
                                            .font(.system(size: textFontSize, weight: isActiveAnim ? .semibold : .regular))
                                            .animation(nil, value: isActiveAnim)
                                            .foregroundColor(isActiveAnim ? .white : Color(.label))
                                            .truncationMode(.tail)
                                            .lineLimit(1)

                                    TriggersListIconsView(triggers: activityUI.data.textTriggers, fontSize: textFontSize - 2)
                                }
                                        .frame(maxWidth: .infinity, alignment: .leading)

                                if let note = activityUI.data.note {
                                    HStack {

                                        let noteFontSize = 15.0

                                        Text(note)
                                                .foregroundColor(.white)
                                                .font(.system(size: noteFontSize, weight: .light))

                                        TriggersListIconsView(triggers: activityUI.data.noteTriggers, fontSize: noteFontSize - 2)
                                    }
                                            .frame(maxWidth: .infinity, alignment: .leading)
                                }
                            }

                            Spacer()

                            ForEach(activityUI.timerHints, id: \.self) { hintUI in
                                Button(
                                        action: {
                                            hintUI.startInterval {}
                                        },
                                        label: {
                                            Text(hintUI.text)
                                                    .offset(y: onePx)
                                                    .font(.system(size: 15, weight: .light))
                                                    .foregroundColor(isActiveAnim ? .white : .blue)
                                                    .padding(.horizontal, 4)
                                        }
                                )
                            }
                        }
                                .padding(.trailing, activitiesInnerHPadding)

                        if let timerData = activityUI.data.timerData {

                            ZStack(alignment: .bottomLeading) {

                                let timerDataTitleLen = timerData.title.count
                                let timerTitleFontWeight: CGFloat = {
                                    if timerDataTitleLen <= 5 { return 38 }
                                    if timerDataTitleLen <= 7 { return 35 }
                                    return 30
                                }()

                                Button(
                                        action: {
                                            vm.toggleIsPurple()
                                        },
                                        label: {
                                            Text(timerData.title)
                                                    .padding(.leading, activitiesInnerHPadding - 1)
                                                    .font(getTimerFont(size: timerTitleFontWeight))
                                                    .foregroundColor(.white)
                                        }
                                )

                                HStack {

                                    Spacer()

                                    HStack {

                                        let timerButtonsHeight = 30.0

                                        Button(
                                                action: {
                                                    activityUI.pauseLastInterval()
                                                },
                                                label: {
                                                    Image(systemName: "pause")
                                                            .foregroundColor(.white)
                                                            .font(.system(size: 14, weight: .regular))
                                                }
                                        )
                                                .frame(width: timerButtonsHeight, height: timerButtonsHeight)
                                                .overlay(roundedShape.stroke(Color.white, lineWidth: 1))

                                        Button(
                                                action: {
                                                    timerData.restart()
                                                },
                                                label: {
                                                    HStack {

                                                        Image(systemName: "clock.arrow.circlepath")
                                                                .foregroundColor(.white)
                                                                .font(.system(size: 16, weight: .light))

                                                        Text(timerData.restartText)
                                                                .padding(.leading, 2)
                                                                .padding(.trailing, 2)
                                                                .font(.system(size: 15, weight: .light))
                                                                .foregroundColor(.white)
                                                    }
                                                }
                                        )
                                                .padding(.horizontal, 6)
                                                .frame(height: timerButtonsHeight)
                                                .overlay(roundedShape.stroke(Color.white, lineWidth: 1))
                                                .padding(.leading, 8)
                                                .padding(.trailing, activitiesInnerHPadding - 2)
                                    }
                                            .padding(.bottom, 1)
                                }
                            }
                                    .padding(.top, 8)
                                    .padding(.bottom, 1)
                        }
                    }
                            .padding(.top, 10)
                            .padding(.bottom, 10)
                            /// #TruncationDynamic + README_APP.md
                            .id("\(activityUI.activity.id) \(lastInterval.note)")
                }
        )
                .sheetEnv(isPresented: $isEditSheetPresented) {
                    ActivityFormSheet(
                            isPresented: $isEditSheetPresented,
                            editedActivity: activityUI.activity
                    ) {
                    }
                }
                .buttonStyle(ActivityButtonStyle(bgColor: bgColorAnim))
    }
}

///
/// Custom cell's implementation because the listRowBackground() hide touch effect
///
/// Dirty magic! While using inside halfSheet the buttons
/// don't work, .buttonStyle(.borderless) on halfSheet helps.
///
private struct ActivityButtonStyle: ButtonStyle {

    let bgColor: Color

    func makeBody(configuration: Self.Configuration) -> some View {
        configuration
                .label
                .background(configuration.isPressed ? Color(.systemGray4) : bgColor)
    }
}

private struct MenuButton: View {

    let sfName: String
    let onClick: () -> Void

    var body: some View {

        Button(
                action: {
                    onClick()
                },
                label: {
                    Image(systemName: sfName)
                            .foregroundColor(.blue)
                            .font(.system(size: 22, weight: .thin))
                }
        )
    }
}
