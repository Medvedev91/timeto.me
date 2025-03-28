import SwiftUI
import shared

let ActivitiesView__listItemHeight = 46.0
let ActivitiesView__timerHintHPadding = 5.0
let ActivitiesView__listEndPadding = 8.0

struct ActivitiesView: View {
    
    var body: some View {
        VmView({
            ActivitiesVm()
        }) { vm, state in
            ActivitiesViewInner(
                vm: vm,
                state: state
            )
        }
    }
}

///

private let activityItemEmojiWidth = 30.0
private let activityItemEmojiHPadding = 8.0
private let activityItemPaddingStart = activityItemEmojiWidth + (activityItemEmojiHPadding * 2)

private struct ActivitiesViewInner: View {
    
    let vm: ActivitiesVm
    let state: ActivitiesVm.State
    
    ///
    
    @EnvironmentObject private var nativeSheet: NativeSheet
    
    var body: some View {
        
        ScrollView {
            
            VStack {
                
                ForEach(state.activitiesUi, id: \.activityDb.id) { activityUi in
                    
                    Button(
                        action: {
                            nativeSheet.showActivityTimerSheet(
                                activity: activityUi.activityDb,
                                timerContext: nil,
                                hideOnStart: true,
                                onStart: {}
                            )
                        },
                        label: {
                            
                            ZStack(alignment: .bottomLeading) { // divider + isActive
                                
                                HStack {
                                    
                                    Text(activityUi.activityDb.emoji)
                                        .frame(width: activityItemEmojiWidth)
                                        .padding(.horizontal, activityItemEmojiHPadding)
                                        .font(.system(size: 22))
                                    
                                    Text(activityUi.text)
                                        .foregroundColor(.primary)
                                        .truncationMode(.tail)
                                        .lineLimit(1)
                                    
                                    Spacer()
                                }
                                .padding(.trailing, ActivitiesView__listEndPadding)
                                .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .leading)
                                
                                if state.activitiesUi.last != activityUi {
                                    // todo
                                    SheetDividerBg()
                                        .padding(.leading, activityItemPaddingStart)
                                }
                                
                                if activityUi.isActive {
                                    ZStack {}
                                        .frame(width: 8, height: ActivitiesView__listItemHeight - 2)
                                        .background(roundedShape.fill(.blue))
                                        .offset(x: -4, y: -1)
                                }
                            }
                        }
                    )
                    .frame(height: ActivitiesView__listItemHeight)
                }
            }
            .fillMaxWidth()
        }
        .defaultScrollAnchor(.bottom)
    }
}
