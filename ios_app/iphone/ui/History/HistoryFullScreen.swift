import SwiftUI
import shared

struct HistoryFullScreen: View {
    
    var body: some View {
        VmView({
            HistoryVm()
        }) { vm, state in
            HistoryFullScreenInner(
                vm: vm,
                state: state
            )
        }
    }
}

///

private struct HistoryFullScreenInner: View {
    
    let vm: HistoryVm
    let state: HistoryVm.State
    
    ///
    
    @Environment(\.dismiss) private var dismiss
    @Environment(Navigation.self) private var navigation
    
    var body: some View {
        
        ScrollViewReader { scrollProxy in
            
            ScrollView(.vertical, showsIndicators: false) {
                
                LazyVStack(spacing: 16, pinnedViews: [.sectionHeaders]) {
                    
                    ForEach(state.daysUi, id: \.unixDay) { dayUi in
                        
                        Section(
                            header: Button(
                                action: {
                                    // todo dialog to move to day
                                    withAnimation {
                                        // scrollPosition.scrollTo(id: "day_20185", anchor: .top)
                                    }
                                },
                                label: {
                                    Text(dayUi.dayText)
                                        .padding(.horizontal, 9)
                                        .padding(.vertical, 6)
                                        .background(Capsule().fill(.blue))
                                        .foregroundColor(.white)
                                        .font(.system(size: 14))
                                }
                            )
                            .id("day_\(dayUi.unixDay)")
                        ) {
                            
                            // Warning
                            // VStack to fix https://developer.apple.com/forums/thread/741406
                            
                            VStack(spacing: 10) {
                                
                                ForEach(dayUi.intervalsUi, id: \.intervalDb.id) { intervalUi in
                                    
                                    HStack(alignment: .top, spacing: 10) {
                                        
                                        HStack(alignment: .center) {
                                            
                                            if !intervalUi.isStartsPrevDay {
                                                
                                                Text(intervalUi.periodString)
                                                    .foregroundColor(.secondary)
                                                    .font(.system(size: 16, weight: .light))
                                                    .frame(alignment: .leading)

                                                Spacer()
                                                
                                                Text(intervalUi.timeString)
                                                    .foregroundColor(.primary)
                                                    .font(.system(size: 15, weight: .semibold, design: .monospaced))
                                                    .frame(alignment: .trailing)
                                            }
                                        }
                                        .frame(width: 130, alignment: .leading)

                                        VStack {
                                            RoundedRectangle(cornerRadius: 20, style: .continuous)
                                                .fill(intervalUi.activityDb.colorRgba.toColor())
                                                .frame(
                                                    width: 10,
                                                    height: Double(10.limitMin(intervalUi.secondsForBar.toInt() / 50))
                                                )
                                                .padding(.leading, 5)
                                                .padding(.trailing, 5)
                                        }
                                        .frame(maxHeight: .infinity)
                                        
                                        VStack(spacing: 4) {
                                            
                                            if !intervalUi.isStartsPrevDay {
                                                
                                                Text(intervalUi.text)
                                                    .foregroundColor(.primary)
                                                    .font(.system(size: 16, weight: .medium))
                                                    .frame(maxWidth: .infinity, alignment: .leading)
                                            }
                                        }
                                        .frame(maxWidth: .infinity)
                                    }
                                    .padding(.leading, 20)
                                    .padding(.trailing, 20)
                                }
                            }
                        }
                    }
                }
            }
            .contentMargins(.bottom, 8)
            .defaultScrollAnchor(.bottom)
        }
        .toolbar {
            ToolbarItemGroup(placement: .bottomBar) {
                BottomBarAddButton(
                    text: "New Entry",
                    action: {
                        // todo
                    }
                )
                Spacer()
                Button("Close") {
                    dismiss()
                }
            }
        }
    }
}
