import SwiftUI
import shared

let ActivitiesView__listItemHeight = 46.0
let ActivitiesView__timerHintHPadding = 5.0
let ActivitiesView__listEndPadding = 8.0

struct ActivitiesView: View {
    
    let timerStrategy: ActivityTimerStrategy
    let presentationMode: PresentationMode
    
    var body: some View {
        VmView({
            ActivitiesVm()
        }) { vm, state in
            ActivitiesViewInner(
                vm: vm,
                state: state,
                timerStrategy: timerStrategy,
                presentationMode: presentationMode
            )
        }
    }
    
    ///

    enum PresentationMode {
        case view
        case sheet
    }
}

///

private let activityItemEmojiWidth = 30.0
private let activityItemEmojiHPadding = 8.0
private let activityItemPaddingStart = activityItemEmojiWidth + (activityItemEmojiHPadding * 2)

private struct ActivitiesViewInner: View {
    
    let vm: ActivitiesVm
    let state: ActivitiesVm.State
    
    let timerStrategy: ActivityTimerStrategy
    let presentationMode: ActivitiesView.PresentationMode

    ///
    
    @Environment(Navigation.self) private var navigation

    var body: some View {
        
        ScrollView {
            
            VStack {
                
                ForEach(state.activitiesUi, id: \.activityDb.id) { activityUi in
                    
                    ZStack(alignment: .bottomLeading) { // divider + isActive
                        
                        Button(
                            action: {
                                navigation.showActivityTimerSheet(
                                    activityDb: activityUi.activityDb,
                                    strategy: timerStrategy,
                                    hideOnStart: presentationMode == .view
                                )
                            },
                            label: {
                                
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
                                    
                                    let timerHintsUi: [ActivitiesVm.TimerHintUi] = activityUi.timerHintsUi
                                    if !timerHintsUi.isEmpty {
                                        ForEach(timerHintsUi, id: \.seconds) { timerHintUi in
                                            Button(
                                                action: {
                                                    timerHintUi.onTap()
                                                },
                                                label: {
                                                    Text(timerHintUi.title)
                                                        .foregroundColor(.blue)
                                                        .padding(.horizontal, ActivitiesView__timerHintHPadding)
                                                        .padding(.vertical, 4)
                                                }
                                            )
                                            .buttonStyle(.borderless)
                                        }
                                    } else {
                                        Button(
                                            action: {
                                                openTimerHintsForm(activityDb: activityUi.activityDb)
                                            },
                                            label: {
                                                Image(systemName: "plus")
                                                    .padding(.horizontal, ActivitiesView__timerHintHPadding)
                                                    .foregroundColor(.secondary)
                                            }
                                        )
                                    }
                                }
                                .padding(.trailing, ActivitiesView__listEndPadding)
                                .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .leading)
                            }
                        )
                        .contextMenu {
                            
                            Button(
                                action: {
                                    navigation.sheet {
                                        ActivityFormSheet(
                                            initActivityDb: activityUi.activityDb
                                        )
                                    }
                                },
                                label: {
                                    Label("Edit", systemImage: "square.and.pencil")
                                }
                            )
                            
                            Button(
                                action: {
                                    openTimerHintsForm(activityDb: activityUi.activityDb)
                                },
                                label: {
                                    Label("Timer Hints", systemImage: "timer")
                                }
                            )
                        }

                        if activityUi.isActive {
                            ZStack {}
                                .frame(width: 8, height: ActivitiesView__listItemHeight - 2)
                                .background(roundedShape.fill(.blue))
                                .offset(x: -4, y: -1)
                        }
                        
                        if state.activitiesUi.last != activityUi {
                            Divider()
                                .padding(.leading, activityItemPaddingStart)
                        }
                    }
                    .frame(height: ActivitiesView__listItemHeight)
                }
            }
            .fillMaxWidth()
        }
        .defaultScrollAnchor(.bottom)
    }
    
    private func openTimerHintsForm(
        activityDb: ActivityDb
    ) {
        navigation.sheet {
            ActivityFormTimerHintsSheet(
                initTimerHints: activityDb.timerHints.toSwift(),
                onDone: { newTimerHints in
                    vm.updateTimerHints(
                        activityDb: activityDb,
                        newTimerHints: newTimerHints.toKotlin()
                    )
                }
            )
        }
    }
}
